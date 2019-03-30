`include "opcodes.v"

module ControlUnit (clk, instruction, microPC, controls, num_inst, is_halted, reset_n);
	input clk;
	input [`WORD_SIZE-1:0]instruction;
	output [2:0]microPC;
	output [12:0]controls;
	output [`WORD_SIZE-1:0] num_inst;
	output is_halted;
	input reset_n;

	reg [2:0]microPC;
	reg [3:0]nextMicroPC;
	reg [12:0]controls;
	reg [`WORD_SIZE-1:0] num_inst;
	reg is_halted;

    // to generate controls, it needs opcode and func.
	wire [3:0]opcode;
	wire [5:0]func;
	assign opcode = instruction[15:12];
	assign func = instruction[5:0];

	initial begin
		microPC = 0;
		nextMicroPC = 0;
		controls = 0;
		num_inst = 0;
		is_halted = 0;
	end

	// when datapath changes instruction, starts decoding
	// also possible with expression below
	// always @(posedge clk) begin
	// 	instruction = 0;
	//  wait (instruction != 0); 

	always @(posedge clk) begin
		if(!reset_n) begin
			microPC = 0;
			nextMicroPC = 0;
			controls = 0;
			num_inst = 0;
			is_halted = 0;
		end
		else if(!is_halted) begin
			microPC = nextMicroPC;
			case (microPC)
				`IF1 : nextMicroPC = `IF2;
				`IF2 : nextMicroPC = `IF3;
				`IF3 : begin
					if (opcode == `JMP_OP || opcode == `JAL_OP) begin // JAL
						nextMicroPC = `EX;
					end else begin // not JAL
						nextMicroPC = `ID;
					end
				end
				`ID : begin
					if (!is_halted) begin	
						if (opcode == 4'd15) begin // R-Type
							if (func == `INST_FUNC_JPR) begin // JPR
								controls[12:5] = 8'b01000000;
								controls[4:1] = `ZERO;
								controls[0] = 1'b0;
							end
							else if (func == `INST_FUNC_JRL) begin // JRL
								controls[12:5] = 8'b01010001;
								controls[4:1] = `ZERO;
								controls[0] = 1'b0;
							end
							else if (func == `INST_FUNC_WWD) begin // WWD
								controls[12] = 1'b1;
								controls[11:0] = 12'b0;
							end
							else if (func == `INST_FUNC_HLT) begin // HLT
								controls[12:0] = 13'b0;
								is_halted = 1'b1;
							end
							else begin //other R-Types
								controls[12:5] = 8'b00000011;
							end
							case (func) 
								`INST_FUNC_ADD : controls[4:1] = `ADD; 
								`INST_FUNC_SUB : controls[4:1] = `SUB;
								`INST_FUNC_AND : controls[4:1] = `AND;
								`INST_FUNC_ORR : controls[4:1] = `OR;
								`INST_FUNC_NOT : controls[4:1] = `NOT;
								`INST_FUNC_TCP : controls[4:1] = `TCP;
								`INST_FUNC_SHL : controls[4:1] = `ALS;
								`INST_FUNC_SHR : controls[4:1] = `ARS;
								default : controls[4:1] = `ZERO;
							endcase
							controls[0] = 1'b0;
						end else if (opcode == `ADI_OP) begin // ADI
							controls[12:5] = 8'b00000001;
							controls[4:1] = `ADD;
							controls[0] = 1'b1;
						end else if (opcode == `ORI_OP) begin // ORI
							controls[12:5] = 8'b00000001;
							controls[4:1] = `OR;
							controls[0] = 1'b1;
						end else if (opcode == `LHI_OP) begin // LHI
							controls[12:5] = 8'b00010001;
							controls[4:1] = `ZERO;
							controls[0] = 1'b0;
						end else if (opcode == `LWD_OP) begin // LWD
							controls[12:5] = 8'b00011001;
							controls[4:1] = `ADD;
							controls[0] = 1'b1;
						end else if (opcode == `SWD_OP) begin // SWD
							controls[12:5] = 8'b00000100;
							controls[4:1] = `ADD;
							controls[0] = 1'b1;
						end else if (opcode >= 4'd0 && opcode <= 4'd3) begin // BNE BEQ BGZ BLZ
							controls[12:5] = 8'b00100000;
							controls[4:1] = `ADD;
							controls[0] = 1'b1;
						end else if (opcode == `JMP_OP) begin // JMP
							controls[12:5] = 8'b01000000;
							controls[4:1] = `ZERO;
							controls[0] = 1'b1;
						end else if (opcode == `JAL_OP) begin // JAL
							controls[12:5] = 8'b01010001;
							controls[4:1] = `ZERO;
							controls[0] = 1'b1;
						end else begin // other case (just in case)
							controls[12:0] = 13'd0;
						end
						nextMicroPC = `EX;
					end
				end
				`EX : begin
					if (opcode ==  4'd15 && func == `INST_FUNC_WWD) begin // WWD
						num_inst = num_inst + 1;
						nextMicroPC = `IF1;
					end else if (opcode == `LWD_OP || opcode == `SWD_OP) begin // L & S
						nextMicroPC = `MEM1;
					end else if (opcode >= 4'd0 && opcode <= 4'd3) begin // BXX
						num_inst = num_inst + 1;
						nextMicroPC = `IF1;
					end else begin
						nextMicroPC = `WB;
					end
				end
				`MEM1 : nextMicroPC = `MEM2;
				`MEM2 : begin
					if (opcode == `LWD_OP) begin // L
						nextMicroPC = `WB;
					end else if (opcode == `SWD_OP) begin // S 
						num_inst = num_inst + 1;
						nextMicroPC = `IF1;
					end
				end
				`WB : begin
					num_inst = num_inst + 1;
					nextMicroPC = `IF1;
				end
			endcase
		end
	end
endmodule
