`include "opcodes.v"
`include "register.v"
`include "ALU.v"
`include "ALUcontrol.v"
`include "forwarding.v"
`include "adder.v"
`include "cache.v"

`define CACHE_LINE 64

module datapath (Clk, Reset_N, readM1, address1, data1, M1busy, readM2, writeM2, address2, data2, M2busy, controls_in, is_halted, IFID_instruction, num_inst, output_port);
	input Clk;
	wire Clk;
	input Reset_N;
	wire Reset_N;

	output readM1;
	wire readM1;
	output [`WORD_SIZE-1:0] address1;
	wire [`WORD_SIZE-1:0] address1;
	output readM2;
	wire readM2;
	output writeM2;
	wire writeM2;
	output [`WORD_SIZE-1:0] address2;
	wire [`WORD_SIZE-1:0] address2;
	input [`CACHE_LINE-1:0] data1;
	wire [`CACHE_LINE-1:0] data1;
	inout [`CACHE_LINE-1:0] data2;
	wire [`CACHE_LINE-1:0] data2;

    input M1busy;
    wire M1busy;
    input M2busy;
    wire M2busy;

    wire C1busy;
    wire C2busy;

    wire [`WORD_SIZE-1:0] cachedata1;
    wire [`WORD_SIZE-1:0] cachedata2;

    wire readC1;
    wire readC2;
    wire writeC2;

    input [10:0]controls_in;
    wire [10:0]controls_in;

    reg [10:0]controls;

    output is_halted;
	reg is_halted;
    output [`WORD_SIZE-1:0]IFID_instruction; // control is produced with instruction in ID stage
    reg [`WORD_SIZE-1:0]IFID_instruction;
	output [`WORD_SIZE-1:0] num_inst;
	reg [`WORD_SIZE-1:0] num_inst;
	output [`WORD_SIZE-1:0] output_port;
	reg [`WORD_SIZE-1:0] output_port;

	reg [`WORD_SIZE-1:0]instruction; // instruciton read from memory
    reg [`WORD_SIZE-1:0]PC; 

    //controls : (WB) WWD[0], RegWrite[1], MemtoReg[2] (MEM) MemWrite[3], MemRead[4] (EX) IsBranch[5], ALUSrc[6], IsALU[7], IsJumpR[8], IsJumpI[9] (ID) RegDst[10]
    // IFID_* means latch values between IF and ID stage.
    // same as IDEX_*, EXMEM_*, MEMWB_*

    reg [`WORD_SIZE-1:0]IFID_PC;
    reg IFID_IsBubble;

    reg [`WORD_SIZE-1:0]IDEX_PC;
    reg [9:0]IDEX_controls;
    reg [`WORD_SIZE-1:0]IDEX_ReadData1;
    reg [`WORD_SIZE-1:0]IDEX_ReadData2;
    reg [1:0]IDEX_rs;
    reg [1:0]IDEX_rt;
    reg [1:0]IDEX_rd;
    reg [3:0]IDEX_opcode;
    reg [5:0]IDEX_func;
    reg [`WORD_SIZE-1:0]IDEX_imm;
    reg IDEX_IsBubble;

    reg [4:0]EXMEM_controls;
    reg [`WORD_SIZE-1:0]EXMEM_ALUOutput; // 
    reg [`WORD_SIZE-1:0]EXMEM_ReadData2; // data from register $rt
                                            // for instructions with data from register $rs, we can use ALUOutput. 
                                            // because in that case, ALUInput2 = 0 and ALUOp = `ADD.
    reg [1:0]EXMEM_rd;
    reg EXMEM_IsBubble;
    reg EXMEM_IsHLT;

    reg [2:0]MEMWB_controls;
    reg [`WORD_SIZE-1:0]MEMWB_ALUOutput;
    reg [`WORD_SIZE-1:0]MEMWB_ReadData;
    reg [1:0]MEMWB_rd;
    reg MEMWB_IsBubble;
    reg MEMWB_IsHLT;

    wire [`WORD_SIZE-1:0]IF_nextPC;
    wire [`WORD_SIZE-1:0]IF_PCAdderOutput;
    wire IF_flush;
    wire IF_stall;

    wire [3:0]ID_opcode;
	wire [1:0]ID_rs;
	wire [1:0]ID_rt;
    wire [1:0]ID_rd;
	wire [5:0]ID_func;
	wire [7:0]ID_imm;
	wire [11:0]ID_target_address;
    wire [`WORD_SIZE-1:0]ID_ReadData1;
    wire [`WORD_SIZE-1:0]ID_ReadData2;
    wire ID_flush;
    wire ID_stall;
    wire ID_use_rs;
    wire ID_use_rt;

    wire [1:0] EX_forwardA;
    wire [1:0] EX_forwardB;
    wire [`WORD_SIZE-1:0]EX_forwardedReadData1;
    wire [`WORD_SIZE-1:0]EX_ALUInput1;
    wire [`WORD_SIZE-1:0]EX_forwardedReadData2;
    wire [`WORD_SIZE-1:0]EX_ALUInput2;
    wire [`WORD_SIZE-1:0]EX_ALUOutput;
    wire [3:0] EX_ALUOp;
    wire EX_OverflowFlag;
    wire EX_bcond; // branch condition
    wire [`WORD_SIZE-1:0]EX_branchPC; // PC when branch condition is True
    wire EX_stall;

    wire MEM_stall;

    wire [`WORD_SIZE-1:0]WB_WriteData;

    assign address1 = PC;
    assign address2 = EXMEM_ALUOutput;

    assign cachedata2 = (EXMEM_controls[3]) ? EXMEM_ReadData2 : `WORD_SIZE'bz;
    assign readC2 = EXMEM_controls[4];
    assign writeC2 = EXMEM_controls[3];
    assign readC1 = !is_halted;
    
    assign IF_nextPC = (MEM_stall == 1'b1 || EX_stall == 1'b1 || ID_stall == 1'b1) ? PC : (IDEX_IsBubble == 1'b0 && EX_bcond == 1'b1 && IDEX_controls[5] == 1'b1) ? EX_branchPC : (IDEX_IsBubble == 1'b0 && IDEX_controls[8] == 1'b1) ? EX_forwardedReadData1 : (IFID_IsBubble == 1'b0 && controls[9] == 1'b1) ? {{PC[15:12]}, {ID_target_address[11:0]}} : (IF_stall == 1'b0) ? IF_PCAdderOutput : PC;
    assign IF_flush = ((IDEX_IsBubble == 1'b0) && ((EX_bcond == 1'b1 && IDEX_controls[5] == 1'b1) || IDEX_controls[8])) || ((IFID_IsBubble == 1'b0) && controls[9] == 1'b1);

    assign ID_flush = (IDEX_IsBubble == 1'b0) && ((EX_bcond == 1'b1 && IDEX_controls[5] == 1'b1) || IDEX_controls[8]);
   
    assign ID_opcode = IFID_instruction[15:12];
    assign ID_rs = IFID_instruction[11:10];
    assign ID_rt = IFID_instruction[9:8];
    assign ID_rd = ((controls[9] || controls[8]) && controls[1]) ? 2'b10 : (controls[10]) ? IFID_instruction[7:6] : ID_rt;
    assign ID_func = IFID_instruction[5:0];
    assign ID_imm = IFID_instruction[7:0];
    assign ID_target_address = IFID_instruction[11:0];

    assign ID_use_rs = (IFID_IsBubble == 1'b1) ? 1'b0 : (ID_opcode == `JMP_OP || ID_opcode == `JAL_OP) ? 1'b0 : 1'b1;
    assign ID_use_rt = (IFID_IsBubble == 1'b1) ? 1'b0 : ((ID_opcode == 4'd15 && ID_func > 6'd24) || (ID_opcode > 4'd1 && ID_opcode != 4'd8 && ID_opcode != 4'd15) || ID_use_rs == 1'b0) ? 1'b0 : 1'b1;

    assign EX_stall = (IDEX_IsBubble == 1'b0 && C1busy == 1'b1 && (IDEX_controls[8] || (IDEX_controls[5] && EX_bcond == 1'b1)));
    assign EX_forwardedReadData1 = (EX_forwardA == 2'b10) ? WB_WriteData : (EX_forwardA == 2'b01) ? EXMEM_ALUOutput : IDEX_ReadData1;
    assign EX_ALUInput1 = (IDEX_controls[1] == 1'b1 && (IDEX_controls[8] == 1'b1 || IDEX_controls[9] == 1'b1)) ? IDEX_PC : EX_forwardedReadData1;
    assign EX_forwardedReadData2 = (EX_forwardB == 2'b10) ? WB_WriteData : (EX_forwardB == 2'b01) ? EXMEM_ALUOutput : IDEX_ReadData2;
    assign EX_ALUInput2 = (IDEX_controls[0] == 1'b1) ? `WORD_SIZE'd0 : (IDEX_controls[1] == 1'b1 && (IDEX_controls[8] == 1'b1 || IDEX_controls[9] == 1'b1)) ? `WORD_SIZE'd0 : ((IDEX_opcode == 4'd15 && IDEX_func == `INST_FUNC_WWD) || IDEX_opcode == 4'd2 || IDEX_opcode == 4'd3) ? `WORD_SIZE'b0 : (IDEX_controls[6]) ? IDEX_imm : EX_forwardedReadData2;
    
    assign WB_WriteData = (MEMWB_controls[2] == 1'b1) ? MEMWB_ReadData : MEMWB_ALUOutput;

    register REGISTER_MODULE(Clk, ID_rs, ID_rt, MEMWB_rd, WB_WriteData, MEMWB_controls[1], ID_ReadData1, ID_ReadData2); 
    ALUcontrol ALUCONTROL_MODULE (EX_ALUOp, IDEX_controls[7], IDEX_opcode, IDEX_func);
	ALU ALU_MODULE (EX_ALUInput1, EX_ALUInput2, EX_ALUOp, EX_ALUOutput, EX_OverflowFlag);
    forwarding FORWARDING_MODULE (EX_forwardA, EX_forwardB, IDEX_rs, IDEX_rt, EXMEM_controls[1], EXMEM_rd, MEMWB_controls[1], MEMWB_rd);
    memorydelay MEMORYDELAY_MODULE(IF_stall, C1busy, readM1, MEM_stall, C2busy, writeM2, readM2, EXMEM_IsBubble);
    hazard HAZARD_MODULE(ID_stall, C1busy, IFID_IsBubble, controls[9], ID_use_rs, ID_rs, ID_use_rt, ID_rt, IDEX_controls[4], IDEX_rd);
    branchcondition BRANCHCONDITION_MODULE (EX_bcond, IDEX_controls[5], IDEX_opcode, EX_ALUOutput);
    adder EX_branchPC_ADDER_MODULE(EX_branchPC, IDEX_PC, IDEX_imm);
    adder PC_ADDER_MODULE(IF_PCAdderOutput, PC, `WORD_SIZE'd1);
    cache CACHE_MODULE(Clk, Reset_N, M1busy, C1busy, data1, cachedata1, readM1, address1, M2busy, C2busy, data2, cachedata2, readM2, writeM2, readC1, readC2, writeC2, address2); 
 

    initial begin
        num_inst = `WORD_SIZE'b0;
        PC = `WORD_SIZE'b0;
        is_halted = 1'b0;
        instruction = `WORD_SIZE'b0;
        output_port = `WORD_SIZE'b0;
        instruction = `WORD_SIZE'b0;
        IFID_IsBubble = 1'b1;
        IDEX_IsBubble = 1'b1;
        EXMEM_IsBubble = 1'b1;
        MEMWB_IsBubble = 1'b1;
        IDEX_controls = 10'b0;
        EXMEM_controls = 5'b0;
        MEMWB_controls = 3'b0;
        EXMEM_IsHLT = 1'b0;
        MEMWB_IsHLT = 1'b0;

        //not important down here. just check that is not x
        IFID_PC = 0;
        IFID_instruction = 0;
        IDEX_PC = 0;
        IDEX_ReadData1 = 0;
        IDEX_ReadData2 = 0;
        IDEX_rs = 0;
        IDEX_rt = 0;
        IDEX_rd = 0;
        IDEX_opcode = 0;
        IDEX_func = 0;
        IDEX_imm = 0;
        EXMEM_ALUOutput = 0;
        EXMEM_ReadData2 = 0;
        EXMEM_rd = 0;
        MEMWB_ALUOutput = 0;
        MEMWB_ReadData = 0;
        MEMWB_rd = 0;
    end

    always @(posedge Clk) begin
        if (is_halted) begin
            output_port = 0;
        end
        else if(!Reset_N) begin
            num_inst = `WORD_SIZE'b0;
            PC = `WORD_SIZE'b0;
            is_halted = 1'b0;
            instruction = `WORD_SIZE'b0;
            IFID_IsBubble = 1'b1;
            IDEX_IsBubble = 1'b1;
            EXMEM_IsBubble = 1'b1;
            MEMWB_IsBubble = 1'b1;
            IDEX_controls = 10'b0;
            EXMEM_controls = 5'b0;
            MEMWB_controls = 3'b0;
            EXMEM_IsHLT = 1'b0;
            MEMWB_IsHLT = 1'b0;

            IFID_PC = 0;
            IFID_instruction = 0;
            IDEX_PC = 0;
            IDEX_ReadData1 = 0;
            IDEX_ReadData2 = 0;
            IDEX_rs = 0;
            IDEX_rt = 0;
            IDEX_rd = 0;
            IDEX_opcode = 0;
            IDEX_func = 0;
            IDEX_imm = 0;
            EXMEM_ALUOutput = 0;
            EXMEM_ReadData2 = 0;
            EXMEM_rd = 0;
            MEMWB_ALUOutput = 0;
            MEMWB_ReadData = 0;
            MEMWB_rd = 0;
        end
        else begin
            // Check if the instruction in WB stage is bubble or not
            // If it is not a bubble and it is a HLT instruction, halt all
            if(MEMWB_IsBubble == 1'b0) begin
                num_inst = num_inst + `WORD_SIZE'd1;
                if (MEMWB_controls[0] == 1'b1) begin
                    output_port = MEMWB_ALUOutput;
                end
                if (MEMWB_IsHLT == 1'b1) begin
                    is_halted = 1'b1;
                    MEMWB_IsBubble = 1'b1;
                    EXMEM_IsBubble = 1'b1;
                    IDEX_IsBubble = 1'b1;
                    IFID_IsBubble = 1'b1;
                end
            end

            controls = controls_in;
            instruction = cachedata1;

            // update PC 
            PC = IF_nextPC;

            // MEMWB Latch
            if (MEM_stall == 1'b0) begin
                MEMWB_ALUOutput = EXMEM_ALUOutput;
                MEMWB_ReadData = cachedata2;
                MEMWB_rd = EXMEM_rd;
                MEMWB_IsBubble = EXMEM_IsBubble;
                MEMWB_controls = EXMEM_controls[2:0];
                MEMWB_IsHLT = EXMEM_IsHLT;
            end
            else begin
                MEMWB_IsBubble = 1'b1;
                MEMWB_controls = 3'b0;
            end

            // EXMEM Latch
            if (MEM_stall == 1'b0 && EX_stall == 1'b0) begin
                if (IDEX_opcode == `LHI_OP) begin
                    EXMEM_ALUOutput = {{IDEX_imm[7:0]}, {8{1'b0}}};
                end
                else begin
                    EXMEM_ALUOutput = EX_ALUOutput;
                end
                EXMEM_ReadData2 = EX_forwardedReadData2;
                EXMEM_rd = IDEX_rd;
                EXMEM_IsBubble = IDEX_IsBubble;
                EXMEM_controls = IDEX_controls[4:0];
                EXMEM_IsHLT = ((IDEX_opcode == `HLT_OP) && (IDEX_func == `INST_FUNC_HLT));
            end
            else if (EX_stall == 1'b1 && MEM_stall == 1'b0) begin
                EXMEM_IsBubble = 1'b1;
                EXMEM_controls = 5'b0;
            end
    
            // IDEX Latch
            if (MEM_stall == 1'b0 && EX_stall == 1'b0 && ID_stall == 1'b0) begin
                IDEX_ReadData1 = ID_ReadData1;
                IDEX_ReadData2 = ID_ReadData2;
                IDEX_rs = ID_rs;
                IDEX_rt = ID_rt;
                IDEX_rd = ID_rd;
                IDEX_opcode = ID_opcode;
                IDEX_func = ID_func;
                IDEX_imm = {{8{ID_imm[7]}}, ID_imm[7:0]};
                IDEX_PC = IFID_PC;
                if(ID_flush) begin
                    IDEX_IsBubble = 1'b1;
                end
                else begin
                    IDEX_IsBubble = IFID_IsBubble;
                end
                if(IDEX_IsBubble == 1'b1) begin
                    IDEX_controls = 10'b0;
                end
                else begin
                    IDEX_controls = controls[9:0];
                end
            end
            else if (ID_stall == 1'b1 && EX_stall == 1'b0 && MEM_stall == 1'b0) begin
                IDEX_IsBubble = 1'b1;
                IDEX_controls = 10'b0;
            end

            // IFID Latch
            if (MEM_stall == 1'b0 && EX_stall == 1'b0 && ID_stall == 1'b0 && IF_stall == 1'b0) begin
                if(IF_flush) begin
                    IFID_IsBubble = 1'b1;
                end
                else begin
                    IFID_instruction = instruction;
                    IFID_IsBubble =1'b0;
                    IFID_PC = IF_PCAdderOutput;
                end
            end
            else if (IF_stall == 1'b1 && ID_stall == 1'b0 && EX_stall == 1'b0 && MEM_stall == 1'b0) begin
                IFID_IsBubble = 1'b1;
                IFID_instruction = `WORD_SIZE'b0;
            end
        end
    end

endmodule