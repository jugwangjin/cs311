`timescale 1ns/1ns
`define PERIOD1 100
`define MEMORY_SIZE 256	//	size of memory is 2^8 words (reduced size)
`define WORD_SIZE 16	//	instead of 2^16 words to reduce memory
			//	requirements in the Active-HDL simulator 

`define LINE_NUMBER 8
`define LINE_SIZE 4
`define TAG_SIZE 11

module Memory(clk, reset_n, readM1, address1, data1, M1busy, readM2, writeM2, address2, data2, M2busy);
	input clk;
	wire clk;
	input reset_n;
	wire reset_n;
	
	input readM1;
	wire readM1;
	input [`WORD_SIZE-1:0] address1;
	wire [`WORD_SIZE-1:0] address1;
	output data1;
	reg [`WORD_SIZE-1:0] data1;
	
	input readM2;
	wire readM2;
	input writeM2;
	wire writeM2;
	input [`WORD_SIZE-1:0] address2;
	wire [`WORD_SIZE-1:0] address2;
	inout data2;
	wire [`WORD_SIZE-1:0] data2;

	output M1busy;
	wire M1busy;
	output M2busy;
	wire M2busy;
	
	reg [`WORD_SIZE-1:0] memory [0:`MEMORY_SIZE-1];
	reg [`WORD_SIZE-1:0] outputData2;

	reg [2:0]M1delay;
	reg [2:0]M2delay;

	// I/D separated cache
	reg [`TAG_SIZE-1:0] i_cache_tag [`LINE_NUMBER-1:0];
	reg i_cache_valid [`LINE_NUMBER-1:0];
	reg [`WORD_SIZE-1:0] i_cache_data [`LINE_NUMBER-1:0][`LINE_SIZE-1:0];

	reg [`TAG_SIZE-1:0] d_cache_tag [`LINE_NUMBER-1:0];
	reg d_cache_valid [`LINE_NUMBER-1:0];
	reg [`WORD_SIZE-1:0] d_cache_data [`LINE_NUMBER-1:0][`LINE_SIZE-1:0];
	reg d_cache_dirty [`LINE_NUMBER-1:0];	

	reg address1_fetching;
	reg [`TAG_SIZE-1:0]address1_tag;
	reg [2:0]address1_index;
	wire i_cache_hit;
	wire i_cache_tag_hit;
	wire [`WORD_SIZE-1:0]i_cache_output;

	reg address2_fetching;
	reg [`TAG_SIZE-1:0]address2_tag;
	reg [2:0]address2_index;
	wire d_cache_hit;
	wire d_cache_tag_hit;
	wire [`WORD_SIZE-1:0]d_cache_output;

	
	assign i_cache_tag_hit = (address1_tag == i_cache_tag[address1_index]);
	assign i_cache_hit = (i_cache_tag_hit && i_cache_valid[address1_index]);
	assign i_cache_output = i_cache_data[address1_index][address1[1:0]];

	assign d_cache_tag_hit = (address2_tag == d_cache_tag[address2_index]);
	assign d_cache_hit = (d_cache_tag_hit && d_cache_valid[address2_index]);
	assign d_cache_output = d_cache_data[address2_index][address2[1:0]];
	
	assign data2 = readM2?outputData2:`WORD_SIZE'bz;
	assign M1busy = (M1delay != 3'b0);
	assign M2busy = (M2delay != 3'b0);

	integer i;
	
	always@(posedge clk)
		if(!reset_n)
			begin
				// init delays and cache
				M1delay = 3'b0;
				M2delay = 3'b0;
				address1_tag = 0;
				address1_index = 0;
				address1_fetching = 0;
				address2_tag = 0;
				address2_index = 0;
				address2_fetching = 0;
				for(i=0; i<`LINE_NUMBER; i=i+1) begin
					i_cache_tag[i] = `TAG_SIZE'b0;
					i_cache_valid[i] = 1'b0;
					i_cache_data[i][0] = `WORD_SIZE'b0;
					i_cache_data[i][1] = `WORD_SIZE'b0;
					i_cache_data[i][2] = `WORD_SIZE'b0;
					i_cache_data[i][3] = `WORD_SIZE'b0;
					d_cache_tag[i] = `TAG_SIZE'b0;
					d_cache_valid[i] = 1'b0;
					d_cache_dirty[i] = 1'b0;
					d_cache_data[i][0] = `WORD_SIZE'b0;
					d_cache_data[i][1] = `WORD_SIZE'b0;
					d_cache_data[i][2] = `WORD_SIZE'b0;
					d_cache_data[i][3] = `WORD_SIZE'b0;
				end
			
				memory[16'h0] <= 16'h9023;
				memory[16'h1] <= 16'h1;
				memory[16'h2] <= 16'hffff;
				memory[16'h3] <= 16'h0;
				memory[16'h4] <= 16'h0;
				memory[16'h5] <= 16'h0;
				memory[16'h6] <= 16'h0;
				memory[16'h7] <= 16'h0;
				memory[16'h8] <= 16'h0;
				memory[16'h9] <= 16'h0;
				memory[16'ha] <= 16'h0;
				memory[16'hb] <= 16'h0;
				memory[16'hc] <= 16'h0;
				memory[16'hd] <= 16'h0;
				memory[16'he] <= 16'h0;
				memory[16'hf] <= 16'h0;
				memory[16'h10] <= 16'h0;
				memory[16'h11] <= 16'h0;
				memory[16'h12] <= 16'h0;
				memory[16'h13] <= 16'h0;
				memory[16'h14] <= 16'h0;
				memory[16'h15] <= 16'h0;
				memory[16'h16] <= 16'h0;
				memory[16'h17] <= 16'h0;
				memory[16'h18] <= 16'h0;
				memory[16'h19] <= 16'h0;
				memory[16'h1a] <= 16'h0;
				memory[16'h1b] <= 16'h0;
				memory[16'h1c] <= 16'h0;
				memory[16'h1d] <= 16'h0;
				memory[16'h1e] <= 16'h0;
				memory[16'h1f] <= 16'h0;
				memory[16'h20] <= 16'h0;
				memory[16'h21] <= 16'h0;
				memory[16'h22] <= 16'h0;
				memory[16'h23] <= 16'h6000;
				memory[16'h24] <= 16'hf01c;
				memory[16'h25] <= 16'h6100;
				memory[16'h26] <= 16'hf41c;
				memory[16'h27] <= 16'h6200;
				memory[16'h28] <= 16'hf81c;
				memory[16'h29] <= 16'h6300;
				memory[16'h2a] <= 16'hfc1c;
				memory[16'h2b] <= 16'h4401;
				memory[16'h2c] <= 16'hf01c;
				memory[16'h2d] <= 16'h4001;
				memory[16'h2e] <= 16'hf01c;
				memory[16'h2f] <= 16'h5901;
				memory[16'h30] <= 16'hf41c;
				memory[16'h31] <= 16'h5502;
				memory[16'h32] <= 16'hf41c;
				memory[16'h33] <= 16'h5503;
				memory[16'h34] <= 16'hf41c;
				memory[16'h35] <= 16'hf2c0;
				memory[16'h36] <= 16'hfc1c;
				memory[16'h37] <= 16'hf6c0;
				memory[16'h38] <= 16'hfc1c;
				memory[16'h39] <= 16'hf1c0;
				memory[16'h3a] <= 16'hfc1c;
				memory[16'h3b] <= 16'hf2c1;
				memory[16'h3c] <= 16'hfc1c;
				memory[16'h3d] <= 16'hf8c1;
				memory[16'h3e] <= 16'hfc1c;
				memory[16'h3f] <= 16'hf6c1;
				memory[16'h40] <= 16'hfc1c;
				memory[16'h41] <= 16'hf9c1;
				memory[16'h42] <= 16'hfc1c;
				memory[16'h43] <= 16'hf1c1;
				memory[16'h44] <= 16'hfc1c;
				memory[16'h45] <= 16'hf4c1;
				memory[16'h46] <= 16'hfc1c;
				memory[16'h47] <= 16'hf2c2;
				memory[16'h48] <= 16'hfc1c;
				memory[16'h49] <= 16'hf6c2;
				memory[16'h4a] <= 16'hfc1c;
				memory[16'h4b] <= 16'hf1c2;
				memory[16'h4c] <= 16'hfc1c;
				memory[16'h4d] <= 16'hf2c3;
				memory[16'h4e] <= 16'hfc1c;
				memory[16'h4f] <= 16'hf6c3;
				memory[16'h50] <= 16'hfc1c;
				memory[16'h51] <= 16'hf1c3;
				memory[16'h52] <= 16'hfc1c;
				memory[16'h53] <= 16'hf0c4;
				memory[16'h54] <= 16'hfc1c;
				memory[16'h55] <= 16'hf4c4;
				memory[16'h56] <= 16'hfc1c;
				memory[16'h57] <= 16'hf8c4;
				memory[16'h58] <= 16'hfc1c;
				memory[16'h59] <= 16'hf0c5;
				memory[16'h5a] <= 16'hfc1c;
				memory[16'h5b] <= 16'hf4c5;
				memory[16'h5c] <= 16'hfc1c;
				memory[16'h5d] <= 16'hf8c5;
				memory[16'h5e] <= 16'hfc1c;
				memory[16'h5f] <= 16'hf0c6;
				memory[16'h60] <= 16'hfc1c;
				memory[16'h61] <= 16'hf4c6;
				memory[16'h62] <= 16'hfc1c;
				memory[16'h63] <= 16'hf8c6;
				memory[16'h64] <= 16'hfc1c;
				memory[16'h65] <= 16'hf0c7;
				memory[16'h66] <= 16'hfc1c;
				memory[16'h67] <= 16'hf4c7;
				memory[16'h68] <= 16'hfc1c;
				memory[16'h69] <= 16'hf8c7;
				memory[16'h6a] <= 16'hfc1c;
				memory[16'h6b] <= 16'h7801;
				memory[16'h6c] <= 16'hf01c;
				memory[16'h6d] <= 16'h7902;
				memory[16'h6e] <= 16'hf41c;
				memory[16'h6f] <= 16'h8901;
				memory[16'h70] <= 16'h8802;
				memory[16'h71] <= 16'h7801;
				memory[16'h72] <= 16'hf01c;
				memory[16'h73] <= 16'h7902;
				memory[16'h74] <= 16'hf41c;
				memory[16'h75] <= 16'h9076;
				memory[16'h76] <= 16'hf01c;
				memory[16'h77] <= 16'h9079;
				memory[16'h78] <= 16'hf01d;
				memory[16'h79] <= 16'hf41c;
				memory[16'h7a] <= 16'hb01;
				memory[16'h7b] <= 16'h907d;
				memory[16'h7c] <= 16'hf01d;
				memory[16'h7d] <= 16'hf01c;
				memory[16'h7e] <= 16'h601;
				memory[16'h7f] <= 16'hf01d;
				memory[16'h80] <= 16'hf41c;
				memory[16'h81] <= 16'h1601;
				memory[16'h82] <= 16'h9084;
				memory[16'h83] <= 16'hf01d;
				memory[16'h84] <= 16'hf01c;
				memory[16'h85] <= 16'h1b01;
				memory[16'h86] <= 16'hf01d;
				memory[16'h87] <= 16'hf41c;
				memory[16'h88] <= 16'h2001;
				memory[16'h89] <= 16'h908b;
				memory[16'h8a] <= 16'hf01d;
				memory[16'h8b] <= 16'hf01c;
				memory[16'h8c] <= 16'h2401;
				memory[16'h8d] <= 16'hf01d;
				memory[16'h8e] <= 16'hf41c;
				memory[16'h8f] <= 16'h2801;
				memory[16'h90] <= 16'h9092;
				memory[16'h91] <= 16'hf01d;
				memory[16'h92] <= 16'hf01c;
				memory[16'h93] <= 16'h3001;
				memory[16'h94] <= 16'hf01d;
				memory[16'h95] <= 16'hf41c;
				memory[16'h96] <= 16'h3401;
				memory[16'h97] <= 16'h9099;
				memory[16'h98] <= 16'hf01d;
				memory[16'h99] <= 16'hf01c;
				memory[16'h9a] <= 16'h3801;
				memory[16'h9b] <= 16'h909d;
				memory[16'h9c] <= 16'hf01d;
				memory[16'h9d] <= 16'hf41c;
				memory[16'h9e] <= 16'ha0af;
				memory[16'h9f] <= 16'hf01c;
				memory[16'ha0] <= 16'ha0ae;
				memory[16'ha1] <= 16'hf01d;
				memory[16'ha2] <= 16'hf41c;
				memory[16'ha3] <= 16'h6300;
				memory[16'ha4] <= 16'h5f03;
				memory[16'ha5] <= 16'h6000;
				memory[16'ha6] <= 16'h4005;
				memory[16'ha7] <= 16'ha0b2;
				memory[16'ha8] <= 16'hf01c;
				memory[16'ha9] <= 16'h90b1;
				memory[16'haa] <= 16'h4900;
				memory[16'hab] <= 16'hf41a;
				memory[16'hac] <= 16'hf01c;
				memory[16'had] <= 16'hf01d;
				memory[16'hae] <= 16'h4a01;
				memory[16'haf] <= 16'hf819;
				memory[16'hb0] <= 16'hf01d;
				memory[16'hb1] <= 16'ha0aa;
				memory[16'hb2] <= 16'h41ff;
				memory[16'hb3] <= 16'h2404;
				memory[16'hb4] <= 16'h6000;
				memory[16'hb5] <= 16'h5001;
				memory[16'hb6] <= 16'hf819;
				memory[16'hb7] <= 16'hf01d;
				memory[16'hb8] <= 16'h8e00;
				memory[16'hb9] <= 16'h8c01;
				memory[16'hba] <= 16'h4f02;
				memory[16'hbb] <= 16'h40fe;
				memory[16'hbc] <= 16'ha0b2;
				memory[16'hbd] <= 16'h7dff;
				memory[16'hbe] <= 16'h8cff;
				memory[16'hbf] <= 16'h44ff;
				memory[16'hc0] <= 16'ha0b2;
				memory[16'hc1] <= 16'h7dff;
				memory[16'hc2] <= 16'h7efe;
				memory[16'hc3] <= 16'hf100;
				memory[16'hc4] <= 16'h4ffe;
				memory[16'hc5] <= 16'hf819;
				memory[16'hc6] <= 16'hf01d;
			end
		else
			// this memory has 2 ports
			// one port is for I memory read, the other port is for D memory read/write
			// so the port for D memory read/write does not do the read/write in parallel.
			begin
				if(M2delay >= 3'd5) begin
					if(d_cache_valid[address2_index] == 1'b0 || d_cache_dirty[address2_index] == 1'b0) begin
						for (i=0; i<`LINE_SIZE; i=i+1) begin
							d_cache_data[address2_index][i[1:0]] = memory[{{address2[`WORD_SIZE-1:2]}, {i[1:0]}}];
						end
						d_cache_valid[address2_index] = 1'b1;
						d_cache_tag[address2_index] = address2_tag;
					end	
					else begin
						for (i=0; i<`LINE_SIZE; i=i+1) begin
							memory[{{d_cache_tag[address2_index]}, {address2_index}, {i[1:0]}}] = d_cache_data[address2_index][i[1:0]];
						end
						d_cache_dirty[address2_index] = 1'b0;
					end
					M2delay = 3'b000;
					address2_fetching = 1'b0;
				end

				if (readM2 == 1'b1 || writeM2 == 1'b1) begin
					if(d_cache_hit == 1'b1) begin
						if(readM2)outputData2 = d_cache_output;
						if(writeM2) begin
							d_cache_data[address2_index][address2[1:0]] = data2;
							d_cache_dirty[address2_index] = 1'b1;
						end
					end
					else begin
						if (address2_fetching == 1'b0) begin
							address2_tag = address2[`WORD_SIZE-1:`WORD_SIZE-`TAG_SIZE];
							address2_index = address2[`WORD_SIZE-`TAG_SIZE-1:`WORD_SIZE-`TAG_SIZE-3];
							address2_fetching = 1'b1;
							M2delay = 3'b001;
						end
						else if (address2_fetching == 1'b1) begin
							M2delay = M2delay + 3'b001;
						end
					end
				end

				if(M1delay >= 3'd5) begin
					for (i=0; i<`LINE_SIZE; i=i+1) begin
						i_cache_data[address1_index][i[1:0]] = memory[{{address1[`WORD_SIZE-1:2]}, {i[1:0]}}];
					end
					i_cache_valid[address1_index] = 1'b1;
					i_cache_tag[address1_index] = address1_tag;
					M1delay = 3'b000;
					address1_fetching = 1'b0;
				end

				if(readM1 == 1'b1) begin
					if(i_cache_hit == 1'b1) begin
						data1 = i_cache_output;
					end
					else begin
						if (address1_fetching == 1'b0) begin
							address1_tag = address1[`WORD_SIZE-1:`WORD_SIZE-`TAG_SIZE];
							address1_index = address1[`WORD_SIZE-`TAG_SIZE-1:`WORD_SIZE-`TAG_SIZE-3];
							address1_fetching = 1'b1;
							M1delay = 3'b001;
						end
						else if (address1_fetching == 1'b1) begin
							M1delay = M1delay + 3'b001;
						end
					end
				end					  
			end
endmodule