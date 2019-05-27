`define TABLE_SIZE 256
`define TAG_SIZE 56
`define INDEX_SIZE 8

module basic_branch_predictor(clk, reset_n, input_ip, output_prediction, input_taken);
	input clk;
	input reset_n;

	input [63:0] input_ip; // 56 bit tag, 8 bit index
	input [0:0] input_taken;
	output [0:0] output_prediction;

	reg [0:0] output_reg;

	reg [`INDEX_SIZE-1:0] i;
	reg input_taken_valid; //pass recent index state update at first clock
	reg [`INDEX_SIZE-1:0] recent_index; //update recent index with input_taken

	reg [`TAG_SIZE-1:0] tag_table [`INDEX_SIZE-1:0];
	reg [1:0] state [`INDEX_SIZE-1:0]; // 00 SNT 01 WNT 10 WT 11 ST

	wire [`TAG_SIZE-1:0] tag;
	wire [`INDEX_SIZE-1:0] index;

	assign output_prediction = output_reg;

	assign tag = input_ip[63:8];
	assign index = input_ip[7:0];

	initial begin
		output_reg <= 0;
		recent_index <= 0;
		input_taken_valid <= 0;
		for (i=0;i<`TABLE_SIZE;i=i+1) begin
			tag_table[i] <= 0;
			state[i] <= 0;
		end
	end

	always @ (*) begin
	end

	always @ (negedge reset_n) begin
		// reset all state asynchronously
		output_reg <= 0;
		recent_index <= 0;
		input_taken_valid <= 0;
		for (i=0;i<`TABLE_SIZE;i=i+1) begin
			tag_table[i] <= 0;
			state[i] <= 0;
		end
	end

	always @ (posedge clk) begin
		if (!input_taken_valid) begin
			input_taken_valid = 1;
		end else begin
			if (state[recent_index]<2'b11 && input_taken) begin
				state[recent_index] = state[recent_index] + 2'b01; 
			end else if (state[recent_index]>2'b00 && !input_taken) begin
				state[recent_index] = state[recent_index] + 2'b01;
			end
		end
		if (tag_table[index] != tag) begin
			tag_table[index] = tag;
			state[index] = 2'b00;
		end
			output_reg = state[index][1];
		recent_index = index;
	end

endmodule
