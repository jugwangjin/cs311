`timescale 1ns/1ns	

`define WORD_SIZE 16
`define BLOCK_SIZE 64


// This is a SAMPLE. You should design your own external_device.
module external_device (external_data, use_bus, idx, dma_begin_interrupt);
	/* inout setting */	
	inout [`BLOCK_SIZE-1:0] external_data;
	
	/* input */
	input use_bus;    
	input [3:0] idx;  
    
	/* output */
	output reg dma_begin_interrupt;
	
	/* external device storage */
	reg [`WORD_SIZE-1:0] stored_data [11:0];

	assign external_data[63:48] = use_bus? stored_data[idx]: `WORD_SIZE'bz;
    assign external_data[47:32] = use_bus? stored_data[idx+1]: `WORD_SIZE'bz;
    assign external_data[31:16] = use_bus? stored_data[idx+2]: `WORD_SIZE'bz;
    assign external_data[15:0] = use_bus? stored_data[idx+3]: `WORD_SIZE'bz;

	/* Initialization */
	//assign data = ...
	initial begin
		dma_begin_interrupt <= 0;
		stored_data[0] <= 16'h0000;
		stored_data[1] <= 16'h1111;
		stored_data[2] <= 16'h2222;
		stored_data[3] <= 16'h3333;
		stored_data[4] <= 16'h4444;
		stored_data[5] <= 16'h5555;
		stored_data[6] <= 16'h6666;
		stored_data[7] <= 16'h7777;
		stored_data[8] <= 16'h8888;
		stored_data[9] <= 16'h9999;
		stored_data[10] <= 16'haaaa;
		stored_data[11] <= 16'hbbbb;
	end

	/* Interrupt CPU at some time */
	initial begin
		#100000;
		$display("LOG: Start DMA! #1");
		dma_begin_interrupt = 1;
		#20;						
		dma_begin_interrupt = 0;
		#(185000 - 100000 - 20);
		$display("LOG: Start DMA! #2");
		dma_begin_interrupt = 1;
		#20;
		dma_begin_interrupt = 0;
	end
endmodule
