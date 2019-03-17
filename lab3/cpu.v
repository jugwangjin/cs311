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

	reg [11:0]controls; // 11 Jump, 10 Branch, 9 MemtoReg, 8 MemRead, 7 MemWrite, 6 RegDst, 5 RegWrite, 4:1 [3:0]ALUOp, 0 ALUSrc;

	wire [3:0]opcode;
	wire [5:0]func;

	assign opcode = instruction[15:12];
	assign func = instruction[5:0];

    datapath DATAPATH_MODULE (PC, instruction, address, data, readM, writeM, ackOutput, inputReady, controls, clk);
// Fill it your codes	
// 11 Jump, 10 Branch, 9 MemtoReg, 8 MemRead, 7 MemWrite, 6 RegDst, 5 RegWrite, 4:1 [3:0]ALUOp, 0 ALUSrc;
	always @(instruction) begin
	  if (opcode == 15) begin // R-Type
	    if (func == 25) begin // JPR
			controls[11:5] <= 7'b1000000;
			controls[4:1] <= `ZERO;
			controls[0] <= 1'b0;
		else if (func == 26) begin // JRL
			controls[11:5] <= 7'b1010001;
			controls[4:1] <= `ZERO;
			controls[0] <= 1'b0;
		else begin
			controls[11:5] <= 7'b0000011;
			case (func) 
				INST_FUNC_ADD : controls[4:1] <= `ADD; 
				INST_FUNC_SUB : controls[4:1] <= `SUB;
				INST_FUNC_AND : controls[4:1] <= `AND;
				INST_FUNC_ORR : controls[4:1] <= `OR;
				INST_FUNC_NOT : controls[4:1] <= `NOT;
				INST_FUNC_TCP : controls[4:1] <= `TCP;
				INST_FUNC_SHL : controls[4:1] <= `ALS;
				INST_FUNC_SHR : controls[4:1] <= `ARS;
				default : controls[4:1] <= `ZERO;
			endcase
			controls[0] <= 1'b0;
		end
	  end else if (opcode == 4'd4) begin
	    controls[11:5] <= 7'b0000001;
		controls[4:1] <= `ADD;
		controls[0] <= 1'b1;
	  end else if (opcode == 4'd5) begin
	    controls[11:5] <= 7'b0000001;
		controls[4:1] <= `OR;
		controls[0] <= 1'b1;
	  end else if (opcode == 4'd6) begin
	    controls[11:5] <= 7'b0010001
		controls[4:1] <= `ZERO;
		controls[0] <= 1'b0;
	  end else if (opcode == 4'd7) begin
	    controls[11:5] <= 7'b0011001;
		controls[4:1] <= `ADD;
		controls[0] <= 1'b1;
	  end else if (opcode == 4'd8) begin
	    controls[11:5] <= 7'b0000100;
		controls[4:1] <= `ADD;
		controls[0] <= 1'b1;
	  end else if (opcode >= 4'd0 && opcode <= 4'd3) begin
		controls[11:5] <= 7'b0100000;
		controls[4:1] <= `ADD;
		controls[0] <= 1'b1;
	  end else if (opcode == 9)
	    controls[11:5] <= 7'b1000000;
		controls[4:1] <= `ZERO;
		controls[0] <= 1'b1;
	  end else if (opcode == 10)
	    controls[11:5] <= 7'b1010001;
		controls[4:1] <= `ZERO;
		controls[0] <= 1'b1;
	  end else begin
		controls[11:0] <= 12'd0;
	  end
	end																																		  
endmodule							  																		  