`timescale 1ns / 100ps

`define	NumBits	16

module ALU (A, B, FuncCode, C, OverflowFlag);
	input [`NumBits-1:0] A;
	input [`NumBits-1:0] B;
	input [3:0] FuncCode;
	output [`NumBits-1:0] C;
	output OverflowFlag;

	reg [`NumBits-1:0] C;
	reg OverflowFlag;

	// You can declare any variables as needed.
	/*
		YOUR VARIABLE DECLARATION...
	*/
	parameter ADD = 4'b0000;
	parameter SUB = 4'b0001;
	parameter ID = 4'b0010;
	parameter NOT = 4'b0011;
	parameter AND = 4'b0100;
	parameter OR = 4'b0101;
	parameter NAND = 4'b0110;
	parameter NOR = 4'b0111;	
	
	parameter XOR = 4'b1000;
	parameter XNOR = 4'b1001;
	parameter LLS = 4'b1010;
	parameter LRS = 4'b1011;
	parameter ALS = 4'b1100;
	parameter ARS = 4'b1101;
	parameter TCP = 4'b1110;
	parameter ZERO = 4'b1111;

	initial begin
		C = 0;
		OverflowFlag = 0;
	end   	

	// TODO: You should implement the functionality of ALU!
	// (HINT: Use 'always @(...) begin ... end')
	/*
		YOUR ALU FUNCTIONALITY IMPLEMENTATION...
	*/
	always @(A or B or FuncCode) begin
		OverflowFlag = 0;
		case (FuncCode)
			ADD: begin
				C = A + B;
				//set overflow flag
				if  ((A[`NumBits-1] ^ C[`NumBits-1]) & (B[`NumBits-1] ^ C[`NumBits-1]))
				OverflowFlag = 1;
			end
			SUB: begin
				C = A - B;
				//set underflow flag
				if ((A[`NumBits-1] ^ C[`NumBits-1]) & (B[`NumBits-1] ^~ C[`NumBits-1]))
				OverflowFlag = 1;
			end
			ID: C = A;
			NOT: C = ~A;
			AND: C = A&B ;
			OR: C = A|B ;
			NAND: C = ~(A&B);
			NOR: C = ~(A|B);
			XOR: C = A^B;
			XNOR: C = A^~B;
			LLS: C = A<<1;
			LRS: C = A>>1;
			ALS: C = A<<1;
			ARS: C = {A[`NumBits-1], A[`NumBits-1:1]};
			TCP: C = ~A + 1;
			ZERO: C = 16'h0000;
			default: C = 16'h0000;
		endcase
	end
endmodule

