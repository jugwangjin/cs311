`timescale 1ns/1ns
`define WORD_SIZE 16    // data and address word size

module cpu(clk, reset_n, readM, writeM, address, data, num_inst, output_port, is_halted);
	input clk;
	input reset_n;
	
	output readM;
	output writeM;
	output [`WORD_SIZE-1:0] address;

	inout [`WORD_SIZE-1:0] data;

	output [`WORD_SIZE-1:0] num_inst;		// number of instruction during execution (for debuging & testing purpose)
	output [`WORD_SIZE-1:0] output_port;	// this will be used for a "WWD" instruction
	output is_halted;

	// TODO : Implement your multi-cycle CPU!

	wire [`WORD_SIZE-1:0]instruction;
	wire [12:0]controls;
	wire [3:0]microPC;

	ControlUnit CONTROLUNIT_MODULE(clk, instruction, microPC, controls, num_inst, is_halted, reset_n);
	datapath DATAPATH_MODULE (readM, writeM, instruction, address, data, output_port, microPC, controls, clk, is_halted, reset_n);																																  

endmodule
