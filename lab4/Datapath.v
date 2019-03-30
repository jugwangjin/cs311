`include "opcodes.v"
`include "register.v"
`include "ALU.v"

module datapath (readM, writeM, instruction, address, data, output_port, microPC, controls, clk, is_halted, reset_n);
	output readM;
	output writeM;
	output [`WORD_SIZE-1:0]instruction;
	output [`WORD_SIZE-1:0]address;

	inout [`WORD_SIZE-1:0]data;
	output [`WORD_SIZE-1:0]output_port;

	input [2:0]microPC;
	input [12:0]controls;
	input clk;
	input is_halted;
	input reset_n;

	reg [`WORD_SIZE-1:0]output_port;

    // states inside 
	reg [`WORD_SIZE-1:0]PC;
	reg [`WORD_SIZE-1:0]nextPC;

    // for output
	reg readM;
	reg writeM;
	reg [`WORD_SIZE-1:0]instruction;
	wire [`WORD_SIZE-1:0]data;
	wire [`WORD_SIZE-1:0]address;

    // for data in/out
	reg [`WORD_SIZE-1:0]data_to_reg; // data read from memory, ALU output, ...
	reg [`WORD_SIZE-1:0]data_to_mem; // data to be written to memory, from register.

    // controls decode (just for readibility)
	wire WWD;
	wire Jump;
	wire Branch;
	wire MemtoReg;
	wire MemRead;
	wire MemWrite;
	wire RegDst;
	wire [3:0]ALUOp;
	wire ALUSrc;

	assign WWD = controls[12];
	assign Jump = controls[11];
	assign Branch = controls[10];
	assign MemtoReg = controls[9];
	assign MemRead = controls[8];
	assign MemWrite = controls[7];
	assign RegDst = controls[6];
	assign ALUOp = (microPC == `IF2 || microPC == `IF3) ? `ADD : controls[4:1];
	assign ALUSrc = controls[0];
    
    // for Register
	wire [3:0]opcode;
	wire [1:0]rs;
	wire [1:0]rt;
	wire [1:0]rd;
	wire [1:0]write_register;
	wire [5:0]func;
	wire [7:0]imm;
	wire [11:0]target_address;

	wire [`WORD_SIZE-1:0] ReadData1;
	wire [`WORD_SIZE-1:0] ReadData2;
	wire [`WORD_SIZE-1:0] WriteData;

    // for ALU
	wire [`WORD_SIZE-1:0]ALUInput1;
	wire [`WORD_SIZE-1:0]ALUInput2;
	wire [`WORD_SIZE-1:0]ALUOutput;
	wire OverflowFlag;

    // imm value (for I-type)
	wire [`WORD_SIZE-1:0] ImmSignExtend;

    // assigning register wires
	assign opcode = instruction[15:12];
	assign rs = instruction[11:10];
	assign rt = (Jump == 1) ? 2'b10 : instruction[9:8];
	assign rd = instruction[7:6];
	assign write_register = (RegDst == 1) ? rd : rt;
	assign func = instruction[5:0];
	assign imm = instruction[7:0];
	assign target_address = instruction[11:0];
	assign WriteData = (MemtoReg == 1) ? data_to_reg : ALUOutput;

    // assigning ALU wires
	// reuse ALU to compute normal next PC
	assign ALUInput1 = (microPC == `IF2 || microPC == `IF3) ? PC : (Branch == 1) ? PC : ReadData1;
	assign ALUInput2 = (microPC == `IF2 || microPC == `IF3) ? 1 : (ALUSrc == 1) ? ImmSignExtend : ReadData2;

    // imm value
	assign ImmSignExtend = {{8{imm[7]}}, imm[7:0]};

    // data wire is connected to data_to_mem only when we write to memory.
	assign data = (MemWrite==1) ? data_to_mem : `WORD_SIZE'bz;
	assign address = (microPC == `IF1 || microPC == `IF2) ? PC : ALUOutput;

    // connect and send signals to other modules. 
	register REGISTER_MODULE(clk, rs, rt, write_register, WriteData, microPC, ReadData1, ReadData2); 
	ALU ALU_MODULE (ALUInput1, ALUInput2, ALUOp, ALUOutput, OverflowFlag);

    initial begin
		PC = 0;
		nextPC = 0;
		readM = 0;
		writeM = 0;
		data_to_mem = 0;
		instruction = 0;
    end

	always @(posedge clk) begin
		if (!reset_n) begin
			PC = 0;
			nextPC = 0;
			readM = 0;
			writeM = 0;
			data_to_mem = 0;
			instruction = 0;
		end else if(!is_halted) begin
			readM = 0;
			writeM = 0;
			case (microPC)
				`IF1 : begin
					PC = nextPC;
					readM = 1'b1;
				end
				`IF2 : begin
					instruction = data;
					readM = 1'b0;
				end
				`IF3 : begin
					nextPC = ALUOutput;
				end
				`ID : begin
				 	if (opcode == 15 && func == `INST_FUNC_WWD) begin
						output_port = ReadData1;
					end
				end
				`EX : begin
					case(opcode) 
						`LHI_OP : begin
							data_to_reg = {imm[7:0], {8{1'b0}}};
						end
						`BNE_OP : begin
								if (ReadData1 != ReadData2) begin
									nextPC = ALUOutput;
								end
							end
						`BEQ_OP : begin
								if (ReadData1 == ReadData2) begin
									nextPC = ALUOutput;
								end
						end
						`BGZ_OP : begin
								if (ReadData1 > 0) begin
									nextPC = ALUOutput;
								end
						end
						`BLZ_OP : begin
								if (ReadData1 < 0) begin
									nextPC = ALUOutput;
								end
						end
						`JMP_OP : begin
								nextPC = {nextPC[15:12], target_address[11:0]};
						end
						`JAL_OP : begin
								data_to_reg = PC;
								nextPC = {nextPC[15:12], target_address[11:0]};
						end
						15 : begin
							if (func == `INST_FUNC_JPR) begin
										nextPC = ReadData1;	
							end else if (func == `INST_FUNC_JRL) begin
										data_to_reg = nextPC;
										nextPC = ReadData1;
							end
						end
					endcase
				end
				`MEM1 : begin
					if (MemWrite) begin // L
						readM = 1;			
					end
					else if (MemRead) begin //S
				                data_to_mem = ReadData2;
				                writeM=1;
					end
				end
				`MEM2 : begin
					if (MemWrite) begin // L
				                data_to_reg = data;		
				                readM = 0;
					end
					else if (MemRead) begin
				                writeM = 0;
					end
				end
			endcase
		end
	end
endmodule