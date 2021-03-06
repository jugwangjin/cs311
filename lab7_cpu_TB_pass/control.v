`include "opcodes.v"

module control(Clk, instruction, is_halted, Reset_N, controls);
	input Clk;
	input Reset_N;
    input [`WORD_SIZE-1:0] instruction;
	input is_halted;
    output controls;
    reg [10:0]controls;
    //controls : (WB) WWD[0], RegWrite[1], MemtoReg[2] (MEM) MemWrite[3], MemRead[4], IsBranch[5] (EX) ALUSrc[6], IsALU[7], IsJumpR[8], IsJumpI[9] (ID) RegDst[10]
    // ALUSrc : if true, use imm value 
    // IsALU : if true, R-type ALU instruction. (has func)
    // RegDst : if true, ID_rd will be IDEX_rd. else, ID_rt will be IDEX_rd
    // IsJumpI is jump with imm value, IsJumpR is jump with register value
    // IsJumpI is used in ID Stage, but it is needed to check if this instruction is jump instruction and it writes to register (JAL)

    wire [`WORD_SIZE-1:0]instruction;
	wire [3:0]opcode;
	wire [5:0]func;
    
	assign opcode = instruction[15:12];
	assign func = instruction[5:0];

    initial begin
        controls = 10'b0;
    end

    always @(negedge Clk) begin
        if (!Reset_N) begin 
            controls = 11'b00000000000;
        end
        else if (is_halted) begin
            controls = 11'b00000000000;
        end
        else if (opcode == 4'd15) begin // R-Type
            if (func[5:3] == 3'b0)  begin // ALU Ops (func[5:3] = 0 -> 0~7)
                controls = 11'b10010000010;
            end
            else if (func == `INST_FUNC_WWD) begin
                controls = 11'b00000000001;
            end
            else if (func == `INST_FUNC_JPR) begin
                controls = 11'b00100000000;
            end
            else if (func == `INST_FUNC_JRL) begin
                controls = 11'b00100000010;
            end
            else if (func == `INST_FUNC_HLT) begin
                controls = 11'b00000000000;
            end
            else begin // undefiend
                controls = 11'b00000000000;
            end
        end
        else if (opcode == `ADI_OP) begin
            controls = 11'b00001000010;
        end
        else if (opcode == `ORI_OP) begin
            controls = 11'b00001000010;
        end
        else if (opcode == `LHI_OP) begin
            controls = 11'b00001000010;
        end
        else if (opcode == `LWD_OP) begin
            controls = 11'b00001010110;
        end
        else if (opcode == `SWD_OP) begin
            controls = 11'b00001001000;
        end
        else if (opcode == `BNE_OP) begin
            controls = 11'b00000100000;
        end
        else if (opcode == `BEQ_OP) begin
            controls = 11'b00000100000;
        end
        else if (opcode == `BGZ_OP) begin
            controls = 11'b00000100000;
        end
        else if (opcode == `BLZ_OP) begin
            controls = 11'b00000100000;
        end
        else if (opcode == `JMP_OP) begin
            controls = 11'b01000000000;
        end
        else if (opcode == `JAL_OP) begin
            controls = 11'b01000000010;
        end
        else begin // undefined
            controls = 11'b00000000000;
        end
    end
endmodule
