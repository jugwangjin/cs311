`define TABLE_SIZE 256
`define TAG_SIZE 56
`define INDEX_SIZE 8
// to test other HW budget, change values above (TABLE_SIZE, TAG_SIZE, INDEX_SIZE)
// `TAG_SIZE + `INDEX_SIZE = 64
// `TABLE_SIZE = 2^`INDEX_SIZE
// HW budget
// (`TAG_SIZE + 2) * `TABLE_SIZE
// = ( 66 - `INDEX_SIZE ) * 2^`INDEX_SIZE
// ex) INDEX_SIZE = 8 -> (66-8)*(2^8)= 14848(bits) = 1856(Bytes) = 1.825(KB)


module basic_branch_predictor(clk, reset_n, input_ip, output_prediction, input_taken);
	input clk;
	input reset_n;

	input [63:0] input_ip; // 56 bit tag, 8 bit index
	input [0:0] input_taken;

	input [63:0] input_ip;
	input [0:0] input_taken;
	output [0:0] output_prediction;

	reg [0:0] output_reg;

	// you can add more variables

	assign output_prediction = output_reg;

	integer i;

	reg [63:0] recent_ip; //update recent index with input_taken

	reg [`TAG_SIZE-1:0] tag_table [`TABLE_SIZE-1:0]; 
	reg [1:0] state [`TABLE_SIZE-1:0]; // 00 SNT 01 WNT 10 WT 11 ST

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

	assign input_tag_correct = (tag_table[input_index] == input_tag) ? 1'b1 : 1'b0;
	assign recent_tag_correct = (tag_table[recent_index] == recent_tag) ? 1'b1 : 1'b0;

	initial begin
		output_reg <= 0;
		recent_ip <= 0;
		for (i=0;i<`TABLE_SIZE;i=i+1) begin
			tag_table[i] <= 0;
			state[i] <= 2'b10;
		end
		
		$display("INDEX_SIZE is %d", `INDEX_SIZE);
		$display("HW budget is %d(bits)", (66 - `INDEX_SIZE) * `TABLE_SIZE);
	end

	always @ (negedge reset_n) begin
		// reset all state asynchronously
		output_reg <= 0;
		recent_ip <= 0;
		for (i=0;i<`TABLE_SIZE;i=i+1) begin
			tag_table[i] <= 0;
			state[i] <= 2'b10;
		end
	end

	// when new ip comes in, predict the result
	always @ (input_ip) begin
		// we predict the instruction is taken when tag is correct and state's 1st bit is 1
		output_reg = (input_tag_correct && state[input_index][1]) ? 1'b1 : 1'b0;

		// to update the state at the next clock
		recent_ip = input_ip;
	end

	always @ (posedge clk) begin
		// update state
		if(recent_tag_correct) begin
		// update state with input_taken when recent ip's tag matches with the tag table's value
			if(input_taken && state[recent_index] != 2'b11) begin
				state[recent_index] = state[recent_index] + 2'b01;
			end
			else if(!input_taken && state[recent_index] != 2'b00) begin
				state[recent_index] = state[recent_index] - 2'b01;
			end
		end
		else begin
		// becuase recent ip's tag does not match, we take new value
		// initial value for new tag entry will be 2'b01 when not taken, 2'b10 when taken
			tag_table[recent_index] = recent_tag;
			state[recent_index] = 2'b01 + {{1'b0}, {input_taken}};
		end
	end

endmodule
