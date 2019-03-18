`include "opcodes.v" 	   
`include "register.v"
`include "ALU.v"
`include "ControlUnit.v"

module cpu (readM, writeM, address, data, ackOutput, inputReady, reset_n, clk);
	output readM;									
	output writeM;								
	output [`WORD_SIZE-1:0] address;	
	inout [`WORD_SIZE-1:0] data;		
	input ackOutput;								
	input inputReady;								
	input reset_n;									
	input clk;	

	wire [`WORD_SIZE-1:0]instruction; 

	// cpu module generates control signals (it includes control unit)
	wire [11:0]controls; // 11 Jump, 10 Branch, 9 MemtoReg, 8 MemRead, 7 MemWrite, 6 RegDst, 5 RegWrite, 4:1 [3:0]ALUOp, 0 ALUSrc;

	// send controls to datapath and send instruction to control
	ControlUnit CONTROLUNIT_MODULE(instruction, controls);
  datapath DATAPATH_MODULE (readM, writeM, instruction, address, data, ackOutput, inputReady, controls, clk);																																  
endmodule							  																		  