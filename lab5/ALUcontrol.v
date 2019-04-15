`include "opcodes.v"

module ALUcontrol(ALUFuncCode, IsALU, opcode, func);
	input IsALU;
	input [3:0]opcode;
	input [5:0]func;
    
    output [3:0]ALUFuncCode;
    reg [3:0]ALUFuncCode;
    
    initial begin
        ALUFuncCode = 4'b0000;
    end

    always @(IsALU or opcode or func) begin
        if (IsALU == 1'b1) begin
            case (func) 
                `INST_FUNC_ADD : ALUFuncCode = `ADD; 
                `INST_FUNC_SUB : ALUFuncCode = `SUB;
                `INST_FUNC_AND : ALUFuncCode = `AND;
                `INST_FUNC_NOT : ALUFuncCode = `NOT;
                `INST_FUNC_ORR : ALUFuncCode = `OR;
                `INST_FUNC_TCP : ALUFuncCode = `TCP;
                `INST_FUNC_SHL : ALUFuncCode = `ALS;
                `INST_FUNC_SHR : ALUFuncCode = `ARS;
                `INST_FUNC_WWD : ALUFuncCode = `ADD;
                default : ALUFuncCode = `ZERO;
            endcase
        end
        else begin
            if (opcode == `ADI_OP) begin
                ALUFuncCode = `ADD;
            end
            else if (opcode == `ORI_OP) begin
                ALUFuncCode = `OR;
            end
            else if (opcode == `LWD_OP) begin
                ALUFuncCode = `ADD;
            end
            else if (opcode == `SWD_OP) begin
                ALUFuncCode = `ADD;
            end
            else if (opcode == `BNE_OP) begin
                ALUFuncCode = `SUB;
            end
            else if (opcode == `BEQ_OP) begin
                ALUFuncCode = `SUB;
            end
            else begin
                ALUFuncCode = `ZERO;
            end
        end
    end
endmodule
