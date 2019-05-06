`include "opcodes.v"


module branchcondition (bcond, EX_IsBranch, EX_opcode, EX_ALUOutput);
    output bcond;
    wire bcond;

    input EX_IsBranch;
    input [3:0]EX_opcode;
    input [`WORD_SIZE-1:0]EX_ALUOutput;
    wire EX_IsBranch;
    wire [3:0]EX_opcode;
    wire [`WORD_SIZE-1:0]EX_ALUOutput;
    
    wire bne;
    wire beq;
    wire bgz;
    wire blz;

    assign bne = (EX_ALUOutput == `WORD_SIZE'b0) ? 1'b0 : 1'b1;
    assign beq = !bne;
    assign bgz = (EX_ALUOutput[`WORD_SIZE-1] == 1'b0 && EX_ALUOutput != `WORD_SIZE'b0) ? 1'b1 : 1'b0;
    assign blz = (EX_ALUOutput[`WORD_SIZE-1] == 1'b1) ? 1'b1 : 1'b0;
    
    assign bcond = (EX_IsBranch == 1'b0) ? 1'b0 : (EX_opcode == 2'b00) ? bne : (EX_opcode == 2'b01) ? beq : (EX_opcode == 2'b10) ? bgz : (EX_opcode == 2'b11) ? blz : 1'b0;

endmodule