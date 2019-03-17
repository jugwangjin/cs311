// Title         : vending_machine.v
// Author      : Jae-Eon Jo (Jojaeeon@postech.ac.kr) 
//					   Dongup Kwon (nankdu7@postech.ac.kr) (2015.03.30)

`include "vending_machine_def.v"

module vending_machine (

	clk,							// Clock signal
	reset_n,						// Reset signal (active-low)
	
	i_input_coin,				// coin is inserted.
	i_select_item,				// item is selected.
	i_trigger_return,			// change-return is triggered 
	
	o_available_item,			// Sign of the item availability
	o_output_item,			// Sign of the item withdrawal
	o_return_coin				// Sign of the coin return
);

	// Ports Declaration
	// Do not modify the module interface
	input clk;
	input reset_n;
	
	input [`kNumCoins-1:0] i_input_coin;
	input [`kNumItems-1:0] i_select_item;
	input i_trigger_return;
		
	output [`kNumItems-1:0] o_available_item;
	output [`kNumItems-1:0] o_output_item;
	output [`kNumCoins-1:0] o_return_coin;
 
	// Normally, every output is register,
	//   so that it can provide stable value to the outside.
	reg [`kNumItems-1:0] o_available_item;
	reg [`kNumItems-1:0] o_output_item;
	reg [`kNumCoins-1:0] o_return_coin;
	
	// Net constant values (prefix kk & CamelCase)
	// Please refer the wikepedia webpate to know the CamelCase practive of writing.
	// http://en.wikipedia.org/wiki/CamelCase
	// Do not modify the values.
	wire [31:0] kkItemPrice [`kNumItems-1:0];	// Price of each item
	wire [31:0] kkCoinValue [`kNumCoins-1:0];	// Value of each coin
	assign kkItemPrice[0] = 400;
	assign kkItemPrice[1] = 500;
	assign kkItemPrice[2] = 1000;
	assign kkItemPrice[3] = 2000;
	assign kkCoinValue[0] = 100;
	assign kkCoinValue[1] = 500;
	assign kkCoinValue[2] = 1000;


	// NOTE: integer will never be used other than special usages.
	// Only used for loop iteration.
	// You may add more integer variables for loop iteration.
	integer i, j, k;

	// Internal states. You may add your own net & reg variables.
	reg [`kTotalBits-1:0] current_total;
	reg [`kItemBits-1:0] num_items [`kNumItems-1:0];
	reg [`kCoinBits-1:0] num_coins [`kNumCoins-1:0];
	
	// Next internal states. You may add your own net and reg variables.
	reg [`kTotalBits-1:0] current_total_nxt;
	reg [`kItemBits-1:0] num_items_nxt [`kNumItems-1:0];
	reg [`kCoinBits-1:0] num_coins_nxt [`kNumCoins-1:0];
	
	// Variables. You may add more your own registers.
	reg [`kTotalBits-1:0] input_total, output_total, return_total;
	reg [31:0] waitTime;

	// initiate values
	initial begin
		// TODO: initiate values
		o_available_item = 0;
		o_output_item = 0;
		o_return_coin = 0;
		current_total = 0;
		current_total_nxt = 0;
		for(i=0; i<`kNumItems; i=i+1) begin
			num_items[i] = 0;
			num_items_nxt[i] = 0;
		end
		for(i=0; i<`kNumCoins; i=i+1) begin
			num_coins[i] = 0;
			num_coins_nxt[i] = 0;
		end
		waitTime = 0;
		return_total = 0;
	end

	
	// Combinational logic for the next states
	always @(i_input_coin) begin
		// TODO: current_total_nxt
		// You don't have to worry about concurrent activations in each input vector (or array).
		if(i_input_coin != 0) begin
			waitTime = 0;
		// Calculate the next current_total state.
			for(i=0; i<`kNumCoins; i=i+1) begin
				if (i_input_coin[i] == 1) begin
					current_total = current_total + kkCoinValue[i];
				end
			end	
		end													   
		// TODO: num_items_nxt			
		
		// TODO: num_coins_nxt
		
		// You may add more next states.
		
		for(i=0; i<`kNumItems; i=i+1) begin
			if (kkItemPrice[i] <= current_total) begin
				o_available_item[i] = 1'b1;
			end
			else begin
				o_available_item[i] = 1'b0;
			end
		end
	end

	
	
	// Combinational logic for the outputs
	always @(i_select_item, i_trigger_return) begin
		// TODO: o_available_item
		// TODO: o_output_item
		for(i=0; i<`kNumItems; i=i+1) begin
			if (i_select_item [i] == 1) begin
				if (kkItemPrice[i] <= current_total) begin
					current_total = current_total - kkItemPrice[i];
					o_output_item[i] = 1'b1;
					waitTime = 0;
				end
			end
			else begin
				o_output_item[i] = 1'b0;
			end
		end

		for(i=0; i<`kNumItems; i=i+1) begin
			if (kkItemPrice[i] <= current_total) begin
				o_available_item[i] = 1'b1;
			end
			else begin
				o_available_item[i] = 1'b0;
			end
		end
		// TODO: o_return_coin
		// if(o_return_coin == 0 && return_total > 0) begin
		// 	for (int i=`kNumCoins-1; i>=0; i--) begin
		// 		if(return_total > kkCoinValue[i]) begin
		// 			o_return_coin[i] = 1'b1;
		// 			return_total = return_total - kkCoinValue[i];
		// 		end
		// 	end
 		// end
		if (i_trigger_return == 1) begin
			return_total = current_total;
			current_total = 0;
			o_available_item = 0;
		end
	end
 
	
	
	// Sequential circuit to reset or update the states
	always @(posedge clk) begin
		if (!reset_n) begin
			// TODO: reset all states.
			o_available_item = 0;
			o_output_item = 0;
			o_return_coin = 0;
			current_total = 0;
			current_total_nxt = 0;
			for(i=0; i<`kNumItems; i=i+1) begin
				num_items[i] = 0;
				num_items_nxt[i] = 0;
			end
			for(i=0; i<`kNumCoins; i=i+1) begin
				num_coins[i] = 0;
				num_coins_nxt[i] = 0;
			end
			waitTime = 0;
			return_total = 0;
		end
		else begin
			// TODO: update all states.
			if (waitTime > `kWaitTime) begin
				return_total = current_total;
				o_available_item = 0;
				current_total = 0;
				waitTime = 0;
			end
			if(return_total > 0) begin
				for (i=`kNumCoins-1; i>=0; i=i-1) begin
					if(return_total >= kkCoinValue[i]) begin
						o_return_coin[i] = 1'b1;
						return_total = return_total - kkCoinValue[i];
					end
					else begin
						o_return_coin[i] = 1'b0;
					end
				end
 			end
			if (current_total > 0) begin
				waitTime = waitTime + 1;
			end
		end
	end

endmodule
