`include "opcodes.v"

module forwarding (forwardA, forwardB, IDEX_rs1, IDEX_rs2, EXMEM_RegWrite, EXMEM_rd, MEMWB_RegWrite, MEMWB_rd);
    output [1:0]forwardA; 
    output [1:0]forwardB;
    wire [1:0]forwardA;
    wire [1:0]forwardB;
    // 2'b00 means no forwarding
    // 2'b01 means forwarding from EXMEM latch
    // 2'b10 means forwarding from MEMWB latch
    // 2'b11 is undefined

    input [1:0]IDEX_rs1;
    input [1:0]IDEX_rs2;
    input EXMEM_RegWrite;
    input [1:0]EXMEM_rd;
    input MEMWB_RegWrite;
    input [1:0]MEMWB_rd;
    wire [1:0]IDEX_rs1;
    wire [1:0]IDEX_rs2;
    wire EXMEM_RegWrite;
    wire [1:0]EXMEM_rd;
    wire MEMWB_RegWrite;
    wire [1:0]MEMWB_rd;

    assign forwardA = (IDEX_rs1 == EXMEM_rd) ? 2'b01 : (IDEX_rs1 == MEMWB_rd) ? 2'b10 : 2'b00;
    assign forwardB = (IDEX_rs2 == EXMEM_rd) ? 2'b01 : (IDEX_rs2 == MEMWB_rd) ? 2'b10 : 2'b00;

endmodule