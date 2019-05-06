`include "opcodes.v"

module memorydelay(IF_stall, ID_stall, M1busy, readM1, IDEX_IsBubble, EX_bcond, IDEX_IsBranch, IDEX_IsJumpR, IFID_IsBubble, IFID_IsJumpI, MEM_stall, M2busy, MemWrite, MemRead, EXMEM_IsBubble);
    output MEM_stall;
    wire MEM_stall;
    output IF_stall;
    wire IF_stall;
    output ID_stall;
    wire ID_stall;

    input M1busy;
    wire M1busy;
    input readM1;
    wire readM1;
    input EX_bcond;
    wire EX_bcond;
    input IDEX_IsBranch;
    wire IDEX_IsBranch;
    input IDEX_IsBubble;
    wire IDEX_IsBubble;
    input IDEX_IsJumpR;
    wire IDEX_IsJumpR;
    input IFID_IsBubble;
    wire IFID_IsBubble;
    input IFID_IsJumpI;
    wire IFID_IsJumpI;
    input M2busy;
    wire M2busy;
    input MemWrite;
    wire MemWrite;
    input MemRead;
    wire MemRead;
    input EXMEM_IsBubble;
    wire EXMEM_IsBubble;

    assign MEM_stall = (EXMEM_IsBubble == 1'b0) && ((MemRead == 1'b1 || MemWrite==1'b1) && M2busy == 1'b1);
    assign IF_stall = (M1busy == 1'b1) && (readM1==1'b1 || (IFID_IsJumpI==1'b1 && IFID_IsBubble == 1'b0));
    assign ID_stall = (M1busy == 1'b1) && (IDEX_IsBubble==1'b0) && (IDEX_IsJumpR==1'b1);

endmodule