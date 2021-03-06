`include "opcodes.v"
module register(Clk, ReadRegister1, ReadRegister2, WriteRegister, WriteData, RegWrite, ReadData1, ReadData2); 
    input Clk;
    input [1:0]ReadRegister1;
    input [1:0]ReadRegister2;
    input [1:0]WriteRegister;
    input [`WORD_SIZE-1:0]WriteData;
    input RegWrite;
    output [`WORD_SIZE-1:0]ReadData1;
    output [`WORD_SIZE-1:0]ReadData2;
                                                                                
    reg [`WORD_SIZE-1:0] registers [0:`NUM_REGS-1];
    assign ReadData1 = registers[ReadRegister1];
    assign ReadData2 = registers[ReadRegister2];
    
    integer i;

    initial begin
        for(i=0; i<`NUM_REGS; i=i+1) begin
            registers[i] = 0;
		end
    end

    always @(negedge Clk) begin
        if (RegWrite) begin
            registers[WriteRegister] = WriteData;
        end
    end
endmodule