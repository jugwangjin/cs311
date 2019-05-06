`include "opcodes.v"

module memorydelay(IF_stall, M1busy, readM1, MEM_stall, M2busy, MemWrite, MemRead, EXMEM_IsBubble);
    output MEM_stall;
    wire MEM_stall;
    output IF_stall;
    wire IF_stall;

    input M1busy;
    wire M1busy;
    input readM1;
    wire readM1;
    input M2busy;
    wire M2busy;
    input MemWrite;
    wire MemWrite;
    input MemRead;
    wire MemRead;
    input EXMEM_IsBubble;
    wire EXMEM_IsBubble;

    assign MEM_stall = (EXMEM_IsBubble == 1'b0) && ((MemRead == 1'b1 || MemWrite==1'b1) && M2busy == 1'b1);
    assign IF_stall = readM1==1'b1 && M1busy==1'b1;

endmodule