`include "opcodes.v"
`include "register.v"
`include "ALU.v"
`include "ALUcontrol.v"
`include "forwarding.v"
`include "adder.v"

module datapath (Clk, Reset_N, readM1, address1, data1, readM2, writeM2, address2, data2, controls, is_halted, instruction, num_inst, output_port);
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

	input [`WORD_SIZE-1:0] data1;
	wire [`WORD_SIZE-1:0] data1;
	inout [`WORD_SIZE-1:0] data2;
	wire [`WORD_SIZE-1:0] data2;

    input [9:0]controls;
    wire [9:0]controls;

    output is_halted;
	reg is_halted;
    
    output [`WORD_SIZE-1:0]instruction;
	reg [`WORD_SIZE-1:0]instruction;

	output [`WORD_SIZE-1:0] num_inst;
	reg [`WORD_SIZE-1:0] num_inst;

	output [`WORD_SIZE-1:0] output_port;
	reg [`WORD_SIZE-1:0] output_port;

    reg [`WORD_SIZE-1:0]PC;

    //controls : (WB) WWD[0], RegWrite[1], MemtoReg[2] (MEM) MemWrite[3], MemRead[4] (EX) IsBranch[5], ALUSrc[6], IsALU[7], IsJumpR[8] (ID) IsJumpI[9]

    // IFID_* means latch values between IF and ID stage.
    // same as IDEX_*, EXMEM_*, MEMWB_*
    reg [`WORD_SIZE-1:0]IFID_PC;
    reg [`WORD_SIZE-1:0]IFID_instruction;
    reg IFID_IsBubble;

    reg [`WORD_SIZE-1:0]IDEX_PC;
    reg [8:0]IDEX_controls;
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
    reg [`WORD_SIZE-1:0]EXMEM_ALUOutput;
    reg [`WORD_SIZE-1:0]EXMEM_ReadData2;
    reg [1:0]EXMEM_rd;
    reg EXMEM_IsBubble;

    reg [2:0]MEMWB_controls;
    reg [`WORD_SIZE-1:0]MEMWB_ALUOutput;
    reg [`WORD_SIZE-1:0]MEMWB_ReadData;
    reg [1:0]MEMWB_rd;
    reg MEMWB_IsBubble;


    wire [`WORD_SIZE-1:0]EX_ALUInput1;
    wire [`WORD_SIZE-1:0]EX_forwardedReadData2;
    wire [`WORD_SIZE-1:0]EX_ALUInput2;
    wire [`WORD_SIZE-1:0]EX_ALUOutput;
    wire [3:0] EX_ALUOp;
    wire EX_OverflowFlag;

    wire [`WORD_SIZE-1:0]WB_WriteData;
    assign WB_WriteData = (MEMWB_controls[2] == 1'b1) ? MEMWB_ReadData : MEMWB_ALUOutput;
    wire [`WORD_SIZE-1:0]ID_ReadData1;
    wire [`WORD_SIZE-1:0]ID_ReadData2;

    wire [1:0] EX_forwardA;
    wire [1:0] EX_forwardB;

    assign EX_ALUInput1 = (EX_forwardA == 2'b10) ? WB_WriteData : (EX_forwardA == 2'b01) ? EXMEM_ALUOutput : IDEX_ReadData1;
    assign EX_forwardedReadData2 = (EX_forwardB == 2'b10) ? WB_WriteData : (EX_forwardB == 2'b01) ? EXMEM_ALUOutput : IDEX_ReadData2;
    assign EX_ALUInput2 = ((IDEX_opcode == 4'd15 && IDEX_func == `INST_FUNC_WWD) || IDEX_opcode == 4'd2 || IDEX_opcode == 4'd3) ? `WORD_SIZE'b0 : (IDEX_controls[6]) ? IDEX_imm : EX_forwardedReadData2;

    wire flushIF;
    wire flushID;
    wire bcond; // branch condition
    wire [`WORD_SIZE-1:0]branchPC;
    
    assign flushIF = (bcond == 1'b1 || IDEX_controls[8] == 1'b1 || controls[9]);
    assign flushID = (bcond == 1'b1 || IDEX_controls[8] == 1'b1) ? 1'b1 : 1'b0;
    

    wire [3:0]ID_opcode;
	wire [1:0]ID_rs;
	wire [1:0]ID_rt;
    wire [1:0]ID_rd;
	wire [5:0]ID_func;
	wire [7:0]ID_imm;
	wire [11:0]ID_target_address;

    wire [`WORD_SIZE-1:0]nextPC;
    wire [`WORD_SIZE-1:0]PCAdderOutput;
    wire [`WORD_SIZE-1:0]constantValue4;
    assign constantValue4 = `WORD_SIZE'd4;
    assign nextPC = (bcond == 1'b1 && IDEX_controls[5] == 1'b1) ? branchPC : (IDEX_controls[8] == 1'b1) ? EX_ALUInput1 : (controls[9] == 1'b1) ? ID_target_address : PCAdderOutput;

    wire ID_stall;
    wire ID_use_rs;
    wire ID_use_rt;

    register REGISTER_MODULE(clk, ID_rs, ID_rt, MEMWB_rd, WB_WriteData, MEMWB_controls[1], ID_ReadData1, ID_ReadData2); 
    ALUcontrol ALUCONTROL_MODULE (EX_ALUOp, IDEX_controls[7], IDEX_opcode, IDEX_func);
	ALU ALU_MODULE (EX_ALUInput1, EX_ALUInput2, EX_ALUOp, EX_ALUOutput, EX_OverflowFlag);
    forwarding FORWARDING_MODULE (EX_forwardA, EX_forwardB, IDEX_rs, IDEX_rt, EXMEM_RegWrite, EXMEM_rd, MEMWB_RegWrite, MEMWB_rd);
    adder branchPC_ADDER_MODULE(branchPC, IDEX_PC, IDEX_imm);
    adder PC_ADDER_MODULE(PCAdderOutput, PC, constantValue4);
    hazard HAZARD_MODULE(ID_stall, ID_use_rs, ID_rs, ID_use_rt, ID_rt, IDEX_MemRead, IDEX_rd);
    branchcondition BRANCHCONDITION_MODULE (bcond, IDEX_controls[5], IDEX_opcode, EX_ALUOutput);

    assign ID_use_rs = (ID_opcode == `JMP_OP || ID_opcode == `JAL_OP) ? 1'b0 : 1'b1;
    assign ID_use_rt = ((ID_opcode == 4'd15 && ID_func > 6'd26) || (ID_opcode > 4'd1 || ID_opcode < 4'd8) || ID_use_rs == 1'b1) ? 1'b0 : 1'b1;

    assign address1 = PC;
    assign address2 = EXMEM_ALUOutput;

    assign data2 = (EXMEM_controls[3]) ? EXMEM_ReadData2 : `WORD_SIZE'bz;
    assign readM2 = EXMEM_controls[4];
    assign writeM2 = EXMEM_controls[3];
    assign readM1 = !is_halted;


    assign ID_opcode = IFID_instruction[15:12];
    assign ID_rs = IFID_instruction[11:10];
    assign ID_rt = IFID_instruction[9:8];
    assign ID_rd = ((controls[9] || controls[8]) && controls[1]) ? 2'b10 : (IDEX_opcode == `LHI_OP) ? ID_rt : IFID_instruction[7:6];
    assign ID_func = IFID_instruction[5:0];
    assign ID_imm = IFID_instruction[7:0];
    assign ID_target_address = IFID_instruction[11:0];

    initial begin
        num_inst = `WORD_SIZE'b0;
        PC = `WORD_SIZE'b0;
        is_halted = 1'b0;
        IFID_IsBubble = 1'b1;
        IDEX_IsBubble = 1'b1;
        EXMEM_IsBubble = 1'b1;
        MEMWB_IsBubble = 1'b1;
        IDEX_controls = 9'b0;
        EXMEM_controls = 5'b0;
        MEMWB_controls = 3'b0;
    end

    always @(posedge Clk) begin
        if(!Reset_N) begin
            num_inst = `WORD_SIZE'b0;
            PC = `WORD_SIZE'b0;
            is_halted = 1'b0;
            IFID_IsBubble = 1'b1;
            IDEX_IsBubble = 1'b1;
            EXMEM_IsBubble = 1'b1;
            MEMWB_IsBubble = 1'b1;
            IDEX_controls = 9'b0;
            EXMEM_controls = 5'b0;
            MEMWB_controls = 3'b0;
        end
        else if (!is_halted) begin
            instruction = data1;
        end
    end

    always @(negedge Clk) begin
        // Check if the instruction in WB stage is bubble or not
        if(MEMWB_IsBubble == 1'b0) begin
            num_inst = num_inst + `WORD_SIZE'd1;
            if (MEMWB_controls[0] == 1'b1) begin
                output_port = MEMWB_ALUOutput;
            end
        end

        // MEMWB Latch
        MEMWB_IsBubble = EXMEM_IsBubble;
        MEMWB_controls = EXMEM_controls[2:0];
        MEMWB_ALUOutput = EXMEM_ALUOutput;
        MEMWB_ReadData = data2;
        MEMWB_rd = EXMEM_rd;

        // EXMEM Latch
        EXMEM_IsBubble = IDEX_IsBubble;
        EXMEM_controls = IDEX_controls[4:0];
        if (IDEX_opcode == `LHI_OP) begin
            EXMEM_ALUOutput = {{IDEX_imm[7:0]}, {8{1'b0}}};
        end
        else begin
            EXMEM_ALUOutput = EX_ALUOutput;
        end
        EXMEM_ReadData2 = EX_forwardedReadData2;
        EXMEM_rd = IDEX_rd;

        // IDEX Latch
        if (ID_stall == 1'b0) begin
            if(flushID) begin
                IDEX_IsBubble = 1'b1;
            end
            else begin
                IDEX_IsBubble = IFID_IsBubble;
            end
            if(IDEX_IsBubble == 1'b1) begin
                IDEX_controls = 9'b0;
            end
            else begin
                IDEX_controls = controls[8:0];
            end
            IDEX_PC = IFID_PC;
            IDEX_ReadData1 = ID_ReadData1;
            IDEX_ReadData2 = ID_ReadData2;
            IDEX_rs = ID_rs;
            IDEX_rt = ID_rt;
            IDEX_rd = ID_rd;
            IDEX_opcode = ID_opcode;
            IDEX_func = ID_func;
            IDEX_imm = {{8{ID_imm[7]}}, ID_imm[7:0]};
        end
        else begin
            IDEX_IsBubble = 1'b1;
            IDEX_controls = 9'b0;
        end

        // IFID Latch
        if (is_halted) begin
            IFID_IsBubble = 1'b1;
        end
        else begin
            if (ID_stall == 1'b0) begin
                if(flushIF) begin
                    IFID_IsBubble = 1'b1;
                end
                else begin
                    IFID_IsBubble =1'b0;
                end
                IFID_PC = PC;
                IFID_instruction = instruction;
            end
        end

        // IF PC update
        if(ID_stall == 1'b0) begin
            PC = nextPC;
        end
    end

endmodule