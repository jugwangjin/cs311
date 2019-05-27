`define TABLE_SIZE 256
`define TAG_SIZE 56
`define INDEX_SIZE 8

module basic_branch_predictor(clk, reset_n, input_ip, output_prediction, input_taken);
	input clk;
	input reset_n;

	input [63:0] input_ip; // 56 bit tag, 8 bit index
	input [0:0] input_taken;
	output [0:0] output_prediction;
	wire [0:0] output_prediction;

	integer i;

	reg [63:0] recent_ip; //update recent index with input_taken

	reg [`TAG_SIZE-1:0] tag_table [`INDEX_SIZE-1:0]; 
	reg [1:0] state [`INDEX_SIZE-1:0]; // 00 SNT 01 WNT 10 WT 11 ST

	wire [`TAG_SIZE-1:0] input_tag;
	wire [`INDEX_SIZE-1:0] input_index;
	wire [`TAG_SIZE-1:0] recent_tag;
	wire [`INDEX_SIZE-1:0] recent_index;

	wire input_tag_correct; // input_ip's tag matches the tag table value
	wire recent_tag_correct; // recent_ip's tag matches the tag table value

	assign input_tag = input_ip[63:64-`TAG_SIZE];
	assign input_index = input_ip[`INDEX_SIZE-1:0];
	assign recent_tag = recent_ip[63:64-`TAG_SIZE];
	assign recent_index = recent_ip[`INDEX_SIZE-1:0];

	assign input_tag_correct = (tag_table[input_index] == input_tag);
	assign recent_tag_correct = (tag_table[recent_index] == recent_tag);

	assign output_prediction = (input_tag_correct && state[input_index][1]);

	initial begin
		recent_index <= 0;
		for (i=0;i<`TABLE_SIZE;i=i+1) begin
			tag_table[i] <= `TAG_SIZE'd0;
			state[i] <= 2'd0;
		end
	end

	always @ (negedge reset_n) begin
		// reset all state asynchronously
		recent_index <= 0;
		for (i=0;i<`TABLE_SIZE;i=i+1) begin
			tag_table[i] <= 0;
			state[i] <= 2b'10;
		end
	end

	always @ (posedge clk) begin
		if(recent_tag_correct) begin
			if(input_taken && state[recent_index] != 2'b11) begin
				state[recent_index] = state[recent_index] + 2'b01;
			end
			else if(!input_taken && state[recent_index] != 2'b00) begin
				state[recent_index] = state[recent_index] - 2'b01;
			end
		end
		else begin
			tag_table[recent_index] = recent_tag;
			state[recent_index] = 2'b01 + {{1'b}, {input_taken}};
		end
		
		recent_ip = input_ip;
	end

endmodule
