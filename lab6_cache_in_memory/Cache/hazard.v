`include "opcodes.v"
// computes ID stall
// ID stall is up when RAW dependency with MemRead occurs
// OR, ID stall is up when jump / branch instruction is trying to change PC while I-cache is fetching from memory
module hazard(stall, M1busy, IFID_IsBubble, ID_IsJumpI, use_rs, rs, use_rt, rt, IDEX_MemRead, IDEX_rd);
    output stall;
    wire stall;

    input M1busy;
    wire M1busy;
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
    assign stall = (((rsstall == 1'b1 || rtstall == 1'b1) && IDEX_MemRead == 1'b1) || (M1busy == 1'b1 && IFID_IsBubble == 1'b0 && ID_IsJumpI == 1'b1)) ? 1'b1 : 1'b0;

endmodule