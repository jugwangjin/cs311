`include "opcodes.v" 	   
`include "register.v"
`include "ALU.v"

module cpu (readM, writeM, address, data, ackOutput, inputReady, reset_n, clk);
	output readM;									
	output writeM;								
	output [`WORD_SIZE-1:0] address;	
	inout [`WORD_SIZE-1:0] data;		
	input ackOutput;								
	input inputReady;								
	input reset_n;									
	input clk;	

	reg [`Word_Size:0]instruction; 

	reg [10:0]controls; // Jump, Branch, MemtoReg, MemRead, MemWrite, RegWrite, [3:0]ALUOp, ALUSrc;
	
	wire [3:0]opcode;
	wire [5:0]func;

	assign opcode = instruction[15:12];
	assign func = instruction[5:0];

    datapath DATAPATH_MODULE (PC, instruction, data, address, readM, writeM, ackOutput, inputReady, controls);
// Fill it your codes	

	always @(instruction) begin
	  if (opcode == 15) begin // R-Type
		case (func) 
			INST_FUNC_ADD : controls[4:1] <= ADD; 
			INST_FUNC_SUB : controls[4:1] <= SUB;
			INST_FUNC_AND : controls[4:1] <= AND;
			INST_FUNC_ORR : controls[4:1] <= OR;
			INST_FUNC_NOT : controls[4:1] <= NOT;
			INST_FUNC_TCP : controls[4:1] <= TCP;
			INST_FUNC_SHL : controls[4:1] <= ALS;
			INST_FUNC_SHR : controls[4:1] <= ARS;
			default : controls[4:1] <= ZERO;
		endcase
		RegWrite <= 1;
	  end else begin
		controls[4:1] <= ZERO;
	  end
	  if (opcode[3] == 1'b0 && opcode <= 4'd8) begin
		  controls[0] <= 1;
	  end
	  end else if (opcode == 4'd4) begin
	  	  controls[4:1] <= ADD;
		  RegWrite <= 1;
	  end else if (opcode == 4'd5) begin
		  controls[4:1] <= OR;
		  RegWrite <= 1;
	  end

	end																																				  
endmodule							  																		  