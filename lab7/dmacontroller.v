`include "opcodes.v"
`define LENGTH 4

module DMA_controller(Clk, Reset_N, M2busy, dma_address, BR, BG, use_bus, idx, dma_writeM2); 
    input Clk;
    wire Clk;
    input Reset_N;
    wire Reset_N;

    input M2busy;
    wire M2busy;
    input dma_address;
    wire [`WORD_SIZE-1:0]dma_address;

    output BR;
    reg BR;
    input BG;
    wire BG;
    output dma_end_interrupt;

    output use_bus;
    wire use_bus;
    output idx;
    reg [3:0] idx;
    output dma_writeM2;

    assign use_bus = BG;
    assign dma_end_interrupt = (BG && !M2busy && (idx>`LENGTH));
    assign dma_writeM2 = (BG && !M2busy && (idx<`LENGTH));

    initial begin
        BR = 1'b0;
        idx = 4'd0;
    end
    always @(posedge Clk) begin
        if (BG && !M2busy)
            if(idx < `LENGTH)begin
                BR = 1'b1;
                idx = idx + 4;
            end
            else begin
                BR = 1'b0;
                idx = 4'd0;
            end
        end
    end
endmodule