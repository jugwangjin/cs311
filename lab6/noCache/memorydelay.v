`include "opcodes.v"

module memorydelay(IF_stall, Mbusy, readM1, MEM_stall, MemWrite, MemRead, EXMEM_IsBubble);
    output MEM_stall;
    wire MEM_stall;
    output IF_stall;
    wire IF_stall;

    input Mbusy;
    wire Mbusy;
    input readM1;
    wire readM1;
    input MemWrite;
    wire MemWrite;
    input MemRead;
    wire MemRead;
    input EXMEM_IsBubble;
    wire EXMEM_IsBubble;

    assign MEM_stall = (EXMEM_IsBubble == 1'b0) && ((MemRead == 1'b1 || MemWrite==1'b1) && Mbusy == 1'b1);
    assign IF_stall = readM1==1'b1 && Mbusy==1'b1;

endmodule