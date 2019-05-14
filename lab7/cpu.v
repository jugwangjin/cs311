`timescale 1ns/1ns
`define WORD_SIZE 16    // data and address word size
`define BLOCK_SIZE 64

module cpu(Clk, Reset_N, readM1, address1, data1, M1busy, readM2, cpu_writeM2, cpu_address2, cpu_data2, M2busy, num_inst, output_port, is_halted, dma_begin_interrupt, dma_end_interrupt, BR, BG, dma_address);
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
	output cpu_writeM2;
	wire cpu_writeM2;
	output [`WORD_SIZE-1:0] cpu_address2;
	wire [`WORD_SIZE-1:0] cpu_address2;

	input M1busy;
	wire M1busy;
	input M2busy;
	wire M2busy;

	input [`BLOCK_SIZE-1:0] data1;
	wire [`BLOCK_SIZE-1:0] data1;
	inout [`BLOCK_SIZE-1:0] cpu_data2;
	wire [`BLOCK_SIZE-1:0] cpu_data2;

	output [`WORD_SIZE-1:0] num_inst;
	wire [`WORD_SIZE-1:0] num_inst;
	output [`WORD_SIZE-1:0] output_port;
	wire [`WORD_SIZE-1:0] output_port;
	output is_halted;
	wire is_halted;

	input dma_begin_interrupt;
	wire dma_begin_interrupt;

	input BR;
	wire BR;
	output BG;
	wire BG;
	output dma_address;
	wire [`WORD_SIZE-1:0]dma_address;

	wire [`WORD_SIZE-1:0]instruction;
	wire [10:0]controls;

	// TODO : Implement your pipelined CPU!
	control CONTROL_MODULE(Clk, instruction, is_halted, Reset_N, controls);
	datapath DATAPATH_MODULE (Clk, Reset_N, readM1, address1, data1, M1busy, readM2, cpu_writeM2, cpu_address2, cpu_data2, M2busy, controls, is_halted, instruction, num_inst, output_port, dma_begin_interrupt, BR, BG, dma_address);
endmodule
