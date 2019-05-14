`include "opcodes.v"
`define LENGTH 12

module DMA_controller(Clk, Reset_N, M2busy, dma_set_address, dma_address, BR, BG, use_bus, idx, dma_writeM2, dma_begin_interrupt, dma_end_interrupt); 
    input Clk;
    wire Clk;
    input Reset_N;
    wire Reset_N;

    input M2busy;
    wire M2busy;
    input dma_set_address;
    wire [`WORD_SIZE-1:0]dma_set_address;

    output dma_address;
    wire [`WORD_SIZE-1:0] dma_address;

    output BR;
    reg BR;
    input BG;
    wire BG;
    input dma_begin_interrupt;
    wire dma_begin_interrupt;
    output dma_end_interrupt;
    wire dma_end_interrupt;

    output use_bus;
    wire use_bus;
    output idx;
    reg [3:0] idx;
    output dma_writeM2;

    reg [3:0] nextidx;

    assign dma_end_interrupt = (BG && !M2busy && (idx == `LENGTH));
    assign dma_writeM2 = (BG && (idx < `LENGTH));
    assign dma_address = dma_set_address + idx;
    assign use_bus = BR && !dma_end_interrupt;

    initial begin
        BR = 1'b0;
        idx = 4'd0;
        nextidx = 4'd0;
    end
    always @(posedge dma_begin_interrupt) begin
        BR = 1'b1;
    end
    always @(posedge dma_end_interrupt) begin
        BR = 1'b0;
        idx = 4'd0;
    end

    always @(posedge Clk) begin
        if (BG && !M2busy) begin
            if(idx < `LENGTH)begin
                idx = nextidx;
                nextidx = idx + 4;
            end
        end
    end
endmodule