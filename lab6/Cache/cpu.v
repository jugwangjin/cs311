`timescale 1ns/1ns
`define WORD_SIZE 16    // data and address word size
`define CACHE_LINE 64

module cpu(Clk, Reset_N, readM1, address1, data1, M1busy, readM2, writeM2, address2, data2, M2busy, num_inst, output_port, is_halted);
	input Clk;
	wire Clk;
	input Reset_N;
	wire Reset_N;

	output readM1;
	wire readM1;
	output [`WORD_SIZE-1:0] address1;
	wire [`WORD_SIZE-1:0] address1;
	output readM2;
	wire readM2;
	output writeM2;
	wire writeM2;
	output [`WORD_SIZE-1:0] address2;
	wire [`WORD_SIZE-1:0] address2;

	input M1busy;
	wire M1busy;
	input M2busy;
	wire M2busy;

	input [`CACHE_LINE-1:0] data1;
	wire [`CACHE_LINE-1:0] data1;
	inout [`CACHE_LINE-1:0] data2;
	wire [`CACHE_LINE-1:0] data2;

	output [`WORD_SIZE-1:0] num_inst;
	wire [`WORD_SIZE-1:0] num_inst;
	output [`WORD_SIZE-1:0] output_port;
	wire [`WORD_SIZE-1:0] output_port;
	output is_halted;
	wire is_halted;

	wire [`WORD_SIZE-1:0]instruction;
	wire [10:0]controls;

	// TODO : Implement your pipelined CPU!
	control CONTROL_MODULE(Clk, instruction, is_halted, Reset_N, controls);
	datapath DATAPATH_MODULE (Clk, Reset_N, readM1, address1, data1, M1busy, readM2, writeM2, address2, data2, M2busy, controls, is_halted, instruction, num_inst, output_port);
endmodule
