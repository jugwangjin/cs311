`include "opcodes.v"

module adder(adderoutput, adderinput1, adderinput2);
    output [`WORD_SIZE-1:0]adderoutput;
    reg [`WORD_SIZE-1:0]adderoutput;
    input [`WORD_SIZE-1:0]adderinput1;
    input [`WORD_SIZE-1:0]adderinput2;

    always @(adderinput1 or adderinput2) begin
        adderoutput = adderinput1 + adderinput2;
    end
endmodule