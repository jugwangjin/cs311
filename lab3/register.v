`include "opcodes.v"
module register(clk, ReadRegister1, ReadRegister2, WriteRegister, WriteData, RegWrite, ReadData1, ReadData2); 
    input clk;
    input ReadRegister1;
    input ReadRegister2;
    input WriteRegister;
    input WriteData;
    input RegWrite;
    output ReadData1;
    output ReadData2;

                                                                                                            
    reg [`WORD_SIZE-1:0] registers [0:`NUM_REGS-1];
    assign ReadData1 = registers[ReadRegister1];
    assign ReadData2 = registers[ReadRegister2];
    
    integer i;

    initial begin
        for(i=0; i<`NUM_REGS; i=i+1) begin
            registers[i] = 0;
		end
    end

    always @(negedge clk) begin
        if (RegWrite == 1) begin
            registers[WriteRegister] <= WriteData;
        end
    end
endmodule