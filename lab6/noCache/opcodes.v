		  
// Opcode
`define	ALU_OP	4'd15
`define	ADI_OP	4'd4
`define	ORI_OP	4'd5
`define	LHI_OP	4'd6
`define	RWD_OP	4'd15
`define	WWD_OP	4'd15
`define	LWD_OP	4'd7   		  
`define	SWD_OP	4'd8  
`define	BNE_OP	4'd0
`define	BEQ_OP	4'd1
`define BGZ_OP	4'd2
`define BLZ_OP	4'd3
`define	JMP_OP	4'd9
`define JAL_OP	4'd10
`define	JPR_OP	4'd15
`define	JRL_OP	4'd15
`define	HLT_OP	4'd15
`define	ENI_OP	4'd15
`define	DSI_OP	4'd15

// ALU Function Codes
`define	FUNC_ADD	3'b000
`define	FUNC_SUB	3'b001				 
`define	FUNC_AND	3'b010
`define	FUNC_ORR	3'b011								    
`define	FUNC_NOT	3'b100
`define	FUNC_TCP	3'b101
`define	FUNC_SHL	3'b110
`define	FUNC_SHR	3'b111	

// ALU instruction function codes
`define INST_FUNC_ADD 6'd0
`define INST_FUNC_SUB 6'd1
`define INST_FUNC_AND 6'd2
`define INST_FUNC_ORR 6'd3
`define INST_FUNC_NOT 6'd4
`define INST_FUNC_TCP 6'd5
`define INST_FUNC_SHL 6'd6
`define INST_FUNC_SHR 6'd7
`define INST_FUNC_RWD 6'd27
`define INST_FUNC_WWD 6'd28
`define INST_FUNC_JPR 6'd25
`define INST_FUNC_JRL 6'd26
`define INST_FUNC_HLT 6'd29
`define INST_FUNC_ENI 6'd30
`define INST_FUNC_DSI 6'd31

// micro pc states
`define IF1	3'b000
`define IF2	3'b001
`define IF3	3'b010
`define ID	3'b011
`define EX	3'b100
`define MEM1	3'b101
`define MEM2	3'b110
`define WB	3'b111

// our ALU func code
`define ADD	4'b0000
`define SUB 	4'b0001
`define IDD 	4'b0010
`define NOT 	4'b0011
`define AND 	4'b0100
`define OR 	4'b0101
`define NAND 	4'b0110
`define NOR 	4'b0111	
`define XOR 	4'b1000
`define XNOR 	4'b1001
`define LLS 	4'b1010
`define LRS 	4'b1011
`define ALS 	4'b1100
`define ARS 	4'b1101
`define TCP 	4'b1110
`define ZERO 	4'b1111


`define	WORD_SIZE	16			
`define	NUM_REGS	4