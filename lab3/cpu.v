`include "opcodes.v" 	   
`include "register.v"

module cpu (readM, writeM, address, data, ackOutput, inputReady, reset_n, clk);
	output readM;									
	output writeM;								
	output [`WORD_SIZE-1:0] address;	
	inout [`WORD_SIZE-1:0] data;		
	input ackOutput;								
	input inputReady;								
	input reset_n;									
	input clk;		

	register REGISTER_MODULE (readM, writeM, address, data, ackOutput, inputReady, reset_n, clk);																				

// Fill it your codes	
	always @(posedge clk) begin
	  
	  
	end																																				  
endmodule							  																		  