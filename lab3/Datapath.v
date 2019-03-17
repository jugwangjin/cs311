`include "opcodes.v"
`include "register.v"
`include "ALU.v"

module datapath (readM, writeM, instruction, data, address, ackOutput, inputReady, controls);
    output readM;
    output writeM;
    output instruction;
    inout data;
    input address;
    input ackOutput;
    input inputReady;
    input controls;

    reg readM;
    reg writeM;
    reg [`WORD_SIZE-1:0]data;
    reg [`WORD_SIZE-1:0]address;

    reg [`Word_Size-1:0]PC;
    reg InstructionLoad;

    wire [`WORD_SIZE-1:0]ALUInput1;
    wire [`WORD_SIZE-1:0]ALUInput2;
    wire [`WORD_SIZE-1:0]ALUOutput;
    wire OverflowFlag;
    
    wire [3:0]opcode;
	wire [1:0]rs;
	wire [1:0]rt;
	wire [1:0]rd;
	wire [5:0]func;
	wire [7:0]imm;
	wire [11:0]target_address;

    assign opcode = instruction[15:12];
    assign rs = instruction[11:10];
    assign rt = instruction[9:8];
    assign rd = instruction[7:6];
    assign func = instruction[5:0];
    assign imm = instruction[7:0];
    assign target_address = instruction[11:0];

    wire Jump;
	wire Branch;
	wire MemtoReg;
	wire MemRead;
	wire MemWrite;
	wire RegWrite;
	wire [3:0]ALUOp;
	wire ALUSrc;

	assign Jump = controls[10];
	assign Branch = controls[9];
	assign MemtoReg = controls[8];
	assign MemRead = controls[7];
	assign MemWrite = controls[6];
	assign RegWrite = controls[5];
	assign ALUOp = controls[4:1];
	assign ALUSrc = controls[0];

    wire [`WORD_SIZE-1:0] ReadData1;
    wire [`WORD_SIZE-1:0] ReadData2;
    wire [`WORD_SIZE-1:0] WriteData;
    wire [`WORD_SIZE-1:0] ImmSignExtend;
    
    assign ImmSignExtend = {imm[7], imm[7], imm[7], imm[7], imm[7], imm[7], imm[7], imm[7], imm[7:0]};
    assign ALUInput1 = ReadData1;
    assign ALUInput2 = (ALUSrc == 1) ? ImmSignExtend : ReadData2;
    assign WriteData = (MemtoReg == 1) ? data : ALUOutput;

    register REGISTER_MODULE(rs, rt, rd, WriteData, RegWrite, ReadData1, ReadData2); 
    ALU ALU_MODULE (ALUInput1, ALUInput2, ALUOp, ALUOutput, OverflowFlag);

    always @(posedge clk) begin
        address <= PC;
        readM <= 1'b1;
        InstructionLoad <= 1'b1;
        wait (inputReady == 1'b1);
        instruction <= data;
        InstructionLoad <= 1'b0;
        readM <= 1'b0;
    end
endmodule


// // ALU instruction function codes
// `define INST_FUNC_ADD 6'd0
// `define INST_FUNC_SUB 6'd1
// `define INST_FUNC_AND 6'd2
// `define INST_FUNC_ORR 6'd3
// `define INST_FUNC_NOT 6'd4
// `define INST_FUNC_TCP 6'd5
// `define INST_FUNC_SHL 6'd6
// `define INST_FUNC_SHR 6'd7
// `define INST_FUNC_JPR 6'd25
// `define INST_FUNC_JRL 6'd26