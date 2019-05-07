`include "opcodes.v"

module hazard(stall, IFID_IsBubble, ID_IsJumpI, use_rs, rs, use_rt, rt, IDEX_MemRead, IDEX_rd);
    output stall;
    wire stall;

    input IFID_IsBubble;
    wire IFID_IsBubble;
    input ID_IsJumpI;
    wire ID_IsJumpI;
    input use_rs;
    input [1:0]rs;
    input use_rt;
    input [1:0]rt;
    input [1:0]IDEX_rd;
    input IDEX_MemRead;
    wire use_rs;
    wire [1:0]rs;
    wire use_rt;
    wire [1:0]rt;
    wire [1:0]IDEX_rd;
    wire IDEX_MemRead;

    wire rsstall;
    wire rtstall;
    assign rsstall = (rs == IDEX_rd) && use_rs == 1'b1;
    assign rtstall = (rt == IDEX_rd) && use_rt == 1'b1;

    assign stall = (((rsstall == 1'b1 || rtstall == 1'b1) && IDEX_MemRead == 1'b1) || (IFID_IsBubble == 1'b0 && ID_IsJumpI == 1'b1])) ? 1'b1 : 1'b0;

endmodule