`include "ALU.v"
`include "opcodes.v"

module ControlUnit (instruction, controls);
    input [`WORD_SIZE-1:0]instruction;
    output [11:0]controls;

    reg [11:0]controls;

    // to generate controls, it needs opcode and func.
	wire [3:0]opcode;
	wire [5:0]func;
	assign opcode = instruction[15:12];
	assign func = instruction[5:0];

    initial begin
        controls = 0;
    end

	// when datapath changes instruction, starts decoding
	// also possible with expression below
	// always @(posedge clk) begin
	// 	instruction = 0;
	//  wait (instruction != 0); 
    always @(instruction) begin
        if (opcode == 15) begin // R-Type
            if (func == 25) begin // JPR
                controls[11:5] = 7'b1000000;
                controls[4:1] = `ZERO;
                controls[0] = 1'b0;

            end else if (func == 26) begin // JRL
                controls[11:5] = 7'b1010001;
                controls[4:1] = `ZERO;
                controls[0] = 1'b0;

		    end else begin //other R-Types
                controls[11:5] = 7'b0000011;
                case (func) 
                    `INST_FUNC_ADD : controls[4:1] = `ADD; 
                    `INST_FUNC_SUB : controls[4:1] = `SUB;
                    `INST_FUNC_AND : controls[4:1] = `AND;
                    `INST_FUNC_ORR : controls[4:1] = `OR;
                    `INST_FUNC_NOT : controls[4:1] = `NOT;
                    `INST_FUNC_TCP : controls[4:1] = `TCP;
                    `INST_FUNC_SHL : controls[4:1] = `ALS;
                    `INST_FUNC_SHR : controls[4:1] = `ARS;
                    default : controls[4:1] = `ZERO;
                endcase
                controls[0] = 1'b0;
            end
				end else if (opcode == 4'd4) begin // ADI
					controls[11:5] = 7'b0000001;
					controls[4:1] = `ADD;
					controls[0] = 1'b1;

				end else if (opcode == 4'd5) begin // ORI
					controls[11:5] = 7'b0000001;
					controls[4:1] = `OR;
					controls[0] = 1'b1;

				end else if (opcode == 4'd6) begin // LHI
					controls[11:5] = 7'b0010001;
					controls[4:1] = `ZERO;
					controls[0] = 1'b0;
					
				end else if (opcode == 4'd7) begin // LWD
					controls[11:5] = 7'b0011001;
					controls[4:1] = `ADD;
					controls[0] = 1'b1;

				end else if (opcode == 4'd8) begin // SWD
					controls[11:5] = 7'b0000100;
					controls[4:1] = `ADD;
					controls[0] = 1'b1;

				end else if (opcode >= 4'd0 && opcode <= 4'd3) begin // BNE BEQ BGZ BLZ
					controls[11:5] = 7'b0100000;
					controls[4:1] = `ADD;
					controls[0] = 1'b1;

				end else if (opcode == 9) begin // JMP
					controls[11:5] = 7'b1000000;
					controls[4:1] = `ZERO;
					controls[0] = 1'b1;

				end else if (opcode == 10) begin // JAL
					controls[11:5] = 7'b1010001;
					controls[4:1] = `ZERO;
					controls[0] = 1'b1;

				end else begin // other case (just in case)
					controls[11:0] = 12'd0;
				end
    end
endmodule
