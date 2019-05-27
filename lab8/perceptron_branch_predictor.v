`define HISTORY_LEN 12
`define TABLE_SIZE 256
`define INDEX_SIZE 8
`define THRESHOLD 38
// the best threshold is floor(1.93*history_length + 14);

module perceptron_branch_predictor(clk, reset_n, input_ip, output_prediction, input_taken);
	input clk;
	input reset_n;

	input [63:0] input_ip;
	input [0:0] input_taken;
	output [0:0] output_prediction;

	reg [0:0] output_reg;

	// you can add more variables

	integer i;
	integer j;

	reg [`HISTORY_LEN-1:0]history_register;
	reg [7:0] perceptron [`TABLE_SIZE-1:0][`HISTORY_LEN:0]; // 8 bit integer
	reg [`HISTORY_LEN:0] selected_perceptron;
	reg [7:0] computed_y;
	reg [`INDEX_SIZE:0] recent_index;

	wire [`INDEX_SIZE-1:0]index;
	wire [7:0] abs_computed_y; // to check the THRESHOLD
	wire train;

	assign output_prediction = output_reg;
	assign index = input_ip[`INDEX_SIZE-1:0];
	assign abs_computed_y = (computed_y^{8{computed_y[7]}}) + computed_y[7];
	assign train = (output_reg != input_taken) || (abs_computed_y <= `THRESHOLD);

	initial begin
		output_reg <= 0;
		computed_y <= 0;
		selected_perceptron <= 0;
		recent_index <= 0;
		for (i=0;i<`TABLE_SIZE;i=i+1) begin
			for (j=0; j<=`HISTORY_LEN; j=j+1) begin
				perceptron[i][j] <= 0;
				perceptron[i][j] <= 0;
				perceptron[i][j] <= 0;
				perceptron[i][j] <= 0;
				perceptron[i][j] <= 0;
				perceptron[i][j] <= 0;
				perceptron[i][j] <= 0;
				perceptron[i][j] <= 0;
			end
		end
		history_register = 0;
	end

	always @ (negedge reset_n) begin
		// reset all state asynchronously
		output_reg <= 0;
		computed_y <= 0;
		selected_perceptron <= 0;
		recent_index <= 0;
		for (i=0;i<`TABLE_SIZE;i=i+1)begin
			for (j=0; j<=`HISTORY_LEN; j=j+1)  begin
				perceptron[i][j] <= 0;
				perceptron[i][j] <= 0;
				perceptron[i][j] <= 0;
				perceptron[i][j] <= 0;
				perceptron[i][j] <= 0;
				perceptron[i][j] <= 0;
				perceptron[i][j] <= 0;
				perceptron[i][j] <= 0;
			end
		end
		history_register = 0;
	end

	always @ (posedge clk) begin
		if(train) begin
			for(i=0; i<`HISTORY_LEN; i=i+1) begin
				selected_perceptron[i] = selected_perceptron[i] + {{7{(history_register[i])^(input_taken)}},{1'b1}};
				// update the perceptron
				perceptron[recent_index][i] = selected_perceptron[i];
			end
			// perceptron[`HISTORY_LEN] is the bias weight
			selected_perceptron[`HISTORY_LEN] = selected_perceptron[`HISTORY_LEN] + {{7{!(input_taken)}},{1'b1}};
			perceptron[recent_index][`HISTORY_LEN] = selected_perceptron[`HISTORY_LEN];
		end
		// shift the history register
		// history_register[0] is the latest history
		history_register[`HISTORY_LEN-1:1] = history_register[`HISTORY_LEN-2:0];
		history_register[0] = input_taken;

		computed_y = selected_perceptron[`HISTORY_LEN];
		for(i=0; i<=`HISTORY_LEN; i=i+1) begin
			// fetch from table
			selected_perceptron[i] = perceptron[index][i];
			// compute y
			// if history_register[i] is 0, it will invert all bits in selected perceptron[i] and add !history_register (same as multiply by -1)
			// if history_register[i] is 1, it will do nothing because XOR with all 0's does nothing
			computed_y = computed_y + (selected_perceptron[i]^{8{!history_register[i]}}) + (!history_register[i]);
		end

		// update the recent index, to update perceptron at the next clock
		recent_index = index;
		// non-negative computed_y means output_reg is 1 (taken)
		// negative computed_y (computed_y[7]==1) means output_reg is 0 (not taken)
		output_reg = !computed_y[7];
	end

endmodule
