`include "opcodes.v"
// computes IF stall
module memorydelay(IF_stall, C1busy, readM1, MEM_stall, C2busy, MemWrite, MemRead, EXMEM_IsBubble);
    output MEM_stall;
    wire MEM_stall;
    output IF_stall;
    wire IF_stall;

    input C1busy;
    wire C1busy;
    input readM1;
    wire readM1;
    input C2busy;
    wire C2busy;
    input MemWrite;
    wire MemWrite;
    input MemRead;
    wire MemRead;
    input EXMEM_IsBubble;
    wire EXMEM_IsBubble;

    assign MEM_stall = (EXMEM_IsBubble == 1'b0) && ((MemRead == 1'b1 || MemWrite==1'b1) && C2busy == 1'b1);
    assign IF_stall = readM1==1'b1 && C1busy==1'b1;

endmodule