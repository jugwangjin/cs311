`include "opcodes.v"
`include "register.v"
`include "ALU.v"

module datapath (readM, writeM, instruction, address, data, ackOutput, inputReady, controls, clk);
    output readM;
    output writeM;
    output [`WORD_SIZE-1:0]instruction;
    output [`WORD_SIZE-1:0]address;
    inout [`WORD_SIZE-1:0]data;
    input ackOutput;
    input inputReady;
    input [11:0]controls;
    input clk;

    // states inside 
    reg [`WORD_SIZE-1:0]PC;
    reg RegUpdate; // to notify register that EX stage is done
    reg InstructionFetch; // to check if it is IF stage or not 

    // for output
    reg readM;
    reg writeM;
    reg [`WORD_SIZE-1:0]instruction;
    wire [`WORD_SIZE-1:0]data;
    wire [`WORD_SIZE-1:0]address;
    assign address = (InstructionFetch == 1) ? PC : ALUOutput;

    // for data in/out
    reg [`WORD_SIZE-1:0]data_to_reg; // data read from memory, ALU output, ...
    reg [`WORD_SIZE-1:0]data_to_mem; // data to be written to memory, from register.

    // controls decode (just for readibility)
    wire Jump;
	wire Branch;
	wire MemtoReg;
	wire MemRead;
	wire MemWrite;
    wire RegDst;
	wire RegWrite;
	wire [3:0]ALUOp;
	wire ALUSrc;
	assign Jump = controls[11];
	assign Branch = controls[10];
	assign MemtoReg = controls[9];
	assign MemRead = controls[8];
	assign MemWrite = controls[7];
    assign RegDst = controls[6];
	assign RegWrite = controls[5];
	assign ALUOp = controls[4:1];
	assign ALUSrc = controls[0];
    
    // for Register
    wire [3:0]opcode;
	wire [1:0]rs;
	wire [1:0]rt;
    wire [1:0]rd;
	wire [1:0]write_register;
	wire [5:0]func;
	wire [7:0]imm;
	wire [11:0]target_address;
    wire [`WORD_SIZE-1:0] ReadData1;
    wire [`WORD_SIZE-1:0] ReadData2;
    wire [`WORD_SIZE-1:0] WriteData;
    assign opcode = instruction[15:12];
    assign rs = instruction[11:10];
    assign rt = (Jump == 1) ? 2'b10 : instruction[9:8];
    assign rd = instruction[7:6];
    assign write_register = (RegDst == 1) ? rd : rt;
    assign func = instruction[5:0];
    assign imm = instruction[7:0];
    assign target_address = instruction[11:0];
    assign WriteData = (MemtoReg == 1) ? data_to_reg : ALUOutput;
    
    // imm value (for I-type)
    wire [`WORD_SIZE-1:0] ImmSignExtend;
    assign ImmSignExtend = {{8{imm[7]}}, imm[7:0]};

    // for ALU
    wire [`WORD_SIZE-1:0]ALUInput1;
    wire [`WORD_SIZE-1:0]ALUInput2;
    wire [`WORD_SIZE-1:0]ALUOutput;
    wire OverflowFlag;
    assign ALUInput1 = (Branch == 1) ? PC : ReadData1;
    assign ALUInput2 = (ALUSrc == 1) ? ImmSignExtend : ReadData2;

    // data wire is connected to data_to_mem only when we write to memory.
    assign data = (InstructionFetch!=1 && MemWrite==1) ? data_to_mem : `WORD_SIZE'bz;

    // connect and send signals to other modules. 
    register REGISTER_MODULE(clk, rs, rt, write_register, WriteData, RegWrite, RegUpdate, ReadData1, ReadData2); 
    ALU ALU_MODULE (ALUInput1, ALUInput2, ALUOp, ALUOutput, OverflowFlag);

    initial begin
        PC = 0;
        InstructionFetch = 0;
        readM = 0;
        writeM = 0;
        data_to_reg = 0;
        data_to_mem = 0;
        instruction = 0;
        RegUpdate = 0;
    end

    always @(posedge clk) begin
        RegUpdate = 0;
        // IF
        InstructionFetch = 1'b1;
        readM = 1'b1;
        wait (inputReady == 1'b1);
        readM = 1'b0;
        instruction = data;
        InstructionFetch = 1'b0;
	
        // increment PC
        PC = PC + 1;
        // it needs to wait for cpu to decode instruction
        wait(inputReady==1'b0);

        // EX
        case (opcode) 
            0 : begin
                if (ReadData1 != ReadData2) begin
                    PC = ALUOutput;
                end
            end
            1 : begin
                if (ReadData1 == ReadData2) begin
                    PC = ALUOutput;
                end
            end
            2 : begin
                if (ReadData1 > 0) begin
                    PC = ALUOutput;
                end
            end
            3 : begin
                if (ReadData1 < 0) begin
                    PC = ALUOutput;
                end
            end

            6 : begin
                data_to_reg = {imm[7:0], {8{1'b0}}};
            end

            7 : begin
                readM = 1;
                wait (inputReady == 1'b1);
                readM = 0;
                data_to_reg = data;
		        wait (inputReady == 1'b0);
            end
            8 : begin
                data_to_mem = ReadData2;
                writeM=1;
                wait(ackOutput==1'b1);
                writeM=0;
            end

            9 : begin
                PC = {PC[15:12], target_address[11:0]};
            end
            10 : begin
                data_to_reg = PC;
                PC = {PC[15:12], target_address[11:0]};
            end

            15 : begin
                if (func == 25) begin
                    PC = ReadData1;
                end
                else if (func == 26) begin
                    data_to_reg = PC;
                    PC = ReadData1;
                end
            end
        endcase

        // notify register that ALU job is done. WB 
        RegUpdate = 1;
    end
endmodule