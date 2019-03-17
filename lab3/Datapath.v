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

    reg readM;
    reg writeM;
    reg [`WORD_SIZE-1:0]data_local;
    reg [`WORD_SIZE-1:0]instruction;

    reg [`WORD_SIZE-1:0]data;
    wire [`WORD_SIZE-1:0]address;

    reg [`WORD_SIZE-1:0]PC;
    reg InstructionLoad;

    //controls decode
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

    //for ALU
    wire [`WORD_SIZE-1:0]ALUInput1;
    wire [`WORD_SIZE-1:0]ALUInput2;
    wire [`WORD_SIZE-1:0]ALUOutput;
    wire OverflowFlag;
    
    //for Register
    wire [3:0]opcode;
	wire [1:0]rs;
	wire [1:0]rt;
    wire [1:0]rd;
	wire [1:0]write_register;
	wire [5:0]func;
	wire [7:0]imm;
	wire [11:0]target_address;

    assign opcode = instruction[15:12];
    assign rs = instruction[11:10];
    assign rt = (Jump == 1) ? 2'b10 : instruction[9:8];
    assign rd = instruction[7:6];
    assign write_register = (RegDst == 1) ? rd : rt;
    assign func = instruction[5:0];
    assign imm = instruction[7:0];
    assign target_address = instruction[11:0];

    wire [`WORD_SIZE-1:0] ReadData1;
    wire [`WORD_SIZE-1:0] ReadData2;
    wire [`WORD_SIZE-1:0] WriteData;
    wire [`WORD_SIZE-1:0] ImmSignExtend;
    
    assign ImmSignExtend = {{8{imm[7]}}, imm[7:0]};
    assign ALUInput1 = (Branch == 1) ? PC : ReadData1;
    assign ALUInput2 = (ALUSrc == 1) ? ImmSignExtend : ReadData2;
    assign WriteData = (MemtoReg == 1) ? data_local : ALUOutput;

    assign address = (InstructionLoad == 1) ? PC : ALUOutput;

    register REGISTER_MODULE(clk, rs, rt, write_register, WriteData, RegWrite, ReadData1, ReadData2); 
    ALU ALU_MODULE (ALUInput1, ALUInput2, ALUOp, ALUOutput, OverflowFlag);

    initial begin
        PC = 0;
        InstructionLoad = 0;
        readM = 0;
        writeM = 0;
        data_local = 0;
        instruction = 0;
    end

    always @(posedge clk) begin
        readM <= 1'b1;
        InstructionLoad <= 1'b1;
        wait (inputReady == 1'b1);
        data_local <= data;
        instruction <= data_local;
        
        InstructionLoad <= 1'b0;
        readM <= 1'b0;

        case (opcode) 
            0 : begin
                if (ReadData1 != ReadData2) begin
                    PC <= ALUOutput;
                end
                else begin
                    PC <= PC + 1;
                end
            end
            1 : begin
                if (ReadData1 == ReadData2) begin
                    PC <= ALUOutput;
                end
                else begin
                    PC <= PC + 1;
                end
            end
            2 : begin
                if (ReadData1 > 0) begin
                    PC <= ALUOutput;
                end
                else begin
                    PC <= PC + 1;
                end
            end
            3 : begin
                if (ReadData1 < 0) begin
                    PC <= ALUOutput;
                end
                else begin
                    PC <= PC + 1;
                end
            end
            6 : begin
                data_local <= {imm[7:0], {8{1'b0}}};
            end
            7 : begin
                readM <= 1;
                wait (inputReady == 1'b1);
                data_local <= data;
                readM <= 0;
            end
            8 : begin
                data_local <= ReadData2;
                data <= data_local;
                writeM <= 1;
                wait (ackOutput == 1'b1);
                writeM <= 0;
            end
            9 : begin
                PC <= {PC[15:12], target_address[11:0]};
            end
            10 : begin
                data_local <= PC+1;
                PC <= {PC[15:12], target_address[11:0]};
            end
            15 : begin
                if (func == 25) begin
                    PC <= ReadData1;
                end
                else if (func == 26) begin
                    data_local <= PC + 1;
                    PC <= ReadData1;
                end
            end
        endcase

        if(Jump == 0 && Branch == 0) begin
            PC <= PC+1;
        end
    end
endmodule