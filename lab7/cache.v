`include "opcodes.v"

`define BLOCK_SIZE 64
`define LINE_NUMBER 8
`define LINE_SIZE 4
`define TAG_SIZE 11

module cache(Clk, Reset_N, M1busy, C1busy, data1, cachedata1, readM1, address1, M2busy, C2busy, cpu_data2, cachedata2, readM2, cpu_writeM2, readC1, readC2, writeC2, cpu_address2, BG); 
    input Clk;
    wire Clk;
    input Reset_N;
    wire Reset_N;
    input M1busy;
    wire M1busy;
    output C1busy;
    wire C1busy;
    input data1;
    wire [`BLOCK_SIZE-1:0] data1;
    output cachedata1;
    wire [`WORD_SIZE-1:0] cachedata1;
    output readM1;
    wire readM1;
    input address1;
    wire [`WORD_SIZE-1:0] address1;
    input M2busy;
    wire M2busy;
    output C2busy;
    wire C2busy;
    inout cpu_data2;
    wire [`BLOCK_SIZE-1:0] cpu_data2;
    inout cachedata2;
    wire [`WORD_SIZE-1:0] cachedata2;
    output readM2;
    wire readM2;
    output cpu_writeM2;
    wire cpu_writeM2;
    input readC1;
    wire readC1;
    input readC2;
    wire readC2;
    input writeC2;
    wire writeC2;
    input cpu_address2;
    wire [`WORD_SIZE-1:0]cpu_address2;
    input BG;
    wire BG;

    wire [`WORD_SIZE-1:0]outputcacheData2;

    integer i;

    // I/D separated cache
	reg [`TAG_SIZE-1:0] i_cache_tag [`LINE_NUMBER-1:0];
	reg i_cache_valid [`LINE_NUMBER-1:0];
	reg [`WORD_SIZE-1:0] i_cache_data [`LINE_NUMBER-1:0][`LINE_SIZE-1:0];

	reg [`TAG_SIZE-1:0] d_cache_tag [`LINE_NUMBER-1:0];
	reg d_cache_valid [`LINE_NUMBER-1:0];
	reg [`WORD_SIZE-1:0] d_cache_data [`LINE_NUMBER-1:0][`LINE_SIZE-1:0];
	reg d_cache_dirty [`LINE_NUMBER-1:0];	

	wire [`TAG_SIZE-1:0]address1_tag;
	wire [2:0]address1_index;
	wire i_cache_hit;
	wire i_cache_tag_hit;
	wire [`WORD_SIZE-1:0]i_cache_output;

	wire [`TAG_SIZE-1:0]cpu_address2_tag;
	wire [2:0]cpu_address2_index;
	wire d_cache_hit;
	wire d_cache_tag_hit;
	wire [`WORD_SIZE-1:0]d_cache_output;

	reg [`WORD_SIZE-1:0]hit;
	reg [`WORD_SIZE-1:0]miss;

	assign address1_tag = address1[`WORD_SIZE-1:`WORD_SIZE-`TAG_SIZE];
	assign address1_index = address1[`WORD_SIZE-`TAG_SIZE-1:`WORD_SIZE-`TAG_SIZE-3];
	assign i_cache_tag_hit = (address1_tag == i_cache_tag[address1_index]);
	assign i_cache_hit = (i_cache_tag_hit && i_cache_valid[address1_index]);
	assign i_cache_output = i_cache_data[address1_index][address1[1:0]];

	assign cpu_address2_tag = cpu_address2[`WORD_SIZE-1:`WORD_SIZE-`TAG_SIZE];
	assign cpu_address2_index = cpu_address2[`WORD_SIZE-`TAG_SIZE-1:`WORD_SIZE-`TAG_SIZE-3];
	assign d_cache_tag_hit = (cpu_address2_tag == d_cache_tag[cpu_address2_index]);
	assign d_cache_hit = (d_cache_tag_hit && d_cache_valid[cpu_address2_index]);
	assign d_cache_output = d_cache_data[cpu_address2_index][cpu_address2[1:0]];

    assign readM1 = readC1 && !i_cache_hit;
    assign readM2 = (readC2 || writeC2) && !d_cache_hit && !d_cache_dirty[cpu_address2_index];
    assign cpu_writeM2 = readC2 && !d_cache_hit && d_cache_valid[cpu_address2_index] && d_cache_dirty[cpu_address2_index];

    assign data2[63:48] = (!readM2)?d_cache_data[cpu_address2_index][0]:`WORD_SIZE'bz;
    assign data2[47:32] = (!readM2)?d_cache_data[cpu_address2_index][1]:`WORD_SIZE'bz;
    assign data2[31:16] = (!readM2)?d_cache_data[cpu_address2_index][2]:`WORD_SIZE'bz;
    assign data2[15:0] = (!readM2)?d_cache_data[cpu_address2_index][3]:`WORD_SIZE'bz;

    assign outputcacheData2 = d_cache_output;

    assign cachedata1 = i_cache_output;
    assign cachedata2 = (readC2)?outputcacheData2:`WORD_SIZE'bz;
    
    assign C1busy = !i_cache_hit;
    assign C2busy = !d_cache_hit;

    always @(posedge Clk) begin
        if(!Reset_N) begin
            hit = 0;
            miss = 0;
            for(i=0; i<`LINE_NUMBER; i=i+1) begin
                i_cache_tag[i] = `TAG_SIZE'b0;
                i_cache_valid[i] = 1'b0;
                i_cache_data[i][0] = `WORD_SIZE'b0;
                i_cache_data[i][1] = `WORD_SIZE'b0;
                i_cache_data[i][2] = `WORD_SIZE'b0;
                i_cache_data[i][3] = `WORD_SIZE'b0;
                d_cache_tag[i] = `TAG_SIZE'b0;
                d_cache_valid[i] = 1'b0;
                d_cache_dirty[i] = 1'b0;
                d_cache_data[i][0] = `WORD_SIZE'b0;
                d_cache_data[i][1] = `WORD_SIZE'b0;
                d_cache_data[i][2] = `WORD_SIZE'b0;
                d_cache_data[i][3] = `WORD_SIZE'b0;
            end
        end
        else if(!BG)begin
            if (readC1 && readM1 && !M1busy) begin
                i_cache_data[address1_index][0] = data1[63:48];
                i_cache_data[address1_index][1] = data1[47:32];
                i_cache_data[address1_index][2] = data1[31:16];
                i_cache_data[address1_index][3] = data1[15:0];
                i_cache_valid[address1_index] = 1'b1;
                i_cache_tag[address1_index] = address1_tag;
            end

            if(readC2 && cpu_writeM2 && !M2busy) begin
                d_cache_dirty[cpu_address2_index] = 1'b0;
            end

            if((readC2 || writeC2) && readM2 && !M2busy) begin
                d_cache_data[cpu_address2_index][0] = cpu_data2[63:48];
                d_cache_data[cpu_address2_index][1] = cpu_data2[47:32];
                d_cache_data[cpu_address2_index][2] = cpu_data2[31:16];
                d_cache_data[cpu_address2_index][3] = cpu_data2[15:0];
                d_cache_valid[cpu_address2_index] = 1'b1;
                d_cache_dirty[cpu_address2_index] = 1'b0;
                d_cache_tag[cpu_address2_index] = cpu_address2_tag;
            end

            if(d_cache_hit) begin
                if(writeC2) begin
                    d_cache_data[cpu_address2_index][cpu_address2[1:0]] = cachedata2;
                    d_cache_dirty[cpu_address2_index] = 1'b1;
                end
            end

            if(i_cache_hit) begin
                hit = hit + 1;
                $display("hit: %d", hit)
            end else begin
                miss = miss + 1;
                $display("miss: %d", miss)
            end
            
            if(d_cache_hit) begin
                hit = hit + 1;
                $display("hit: %d", hit)
            end else begin
                miss = miss + 1;
                $display("miss: %d", miss)
            end
        end
    end
endmodule