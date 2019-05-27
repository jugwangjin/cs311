`define HISTORY_LEN 12;
`define TABLE_SIZE 256;
`define INDEX_SIZE 8;


module perceptron_branch_predictor(clk, reset_n, input_ip, output_prediction, input_taken);
	input clk;
	input reset_n;

	input [63:0] input_ip;
	input [0:0] input_taken;
	output [0:0] output_prediction;

	reg [0:0] output_reg;

	// you can add more variables

	reg [`INDEX_SIZE-1:0] i;
	reg input_taken_valid; //pass recent index state update at first clock
	reg [`INDEX_SIZE-1:0] resent_index;

	reg history_register[`HISTORY_LEN:-1:0];
	reg signed [7:0] weight [`INDEX_SIZE-1:0][`HISTORY_LEN:0]; //8-bit signed int
	reg output_value;

	wire [7:0]index;

	assign output_prediction = output_reg;
	assign index = input_ip[7:0];

	initial begin
		output_reg <= 0;
		output_value <= 0;
		input_taken_valid <= 0;
		for (i=0;i<`HISTORY_LEN;i=i+1)begin
			history_register[i] <= 0;
			weight[0][i] <=8'b11111111;
			weight[1][i] <=8'b11111111;
			weight[2][i] <=8'b11111111;
			weight[3][i] <=8'b11111111;
			weight[4][i] <=8'b11111111;
			weight[5][i] <=8'b11111111;
			weight[6][i] <=8'b11111111;
			weight[7][i] <=8'b11111111;
		end
	end

	always @ (negedge reset_n) begin
		// reset all state asynchronously
		output_reg <= 0;
	end

	always @ (posedge clk) begin
		if (!input_taken_valid) begin
			input_taken_valid = 1;
		end else begin
			for (i=0;i<`HISTORY_LEN;i=i+1)begin
				output_value = output_value + history_register[i] * weight[i][index][history_register];
			end
		end

		output_value = weight[`HISTORY_LEN][index];
		for (i=0;i<`HISTORY_LEN;i=i+1)begin
			output_value = output_value + {history_register[i]} * weight[i][index][history_register];
		end
		if (output_value)
	end

endmodule
