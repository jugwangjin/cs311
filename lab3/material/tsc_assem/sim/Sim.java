/*
© 2000, Performance Evaluation Laboratory, Brigham Young University. Reproduction of all or part of this work is permitted for non-profit educational
or research use provided this copyright notice remains intact. All other rights reserved. 
*/

/* THE SIMULATOR */
class Sim extends Thread
{
  /* The opcode of each instruction.  The "opcode" is the actual 
	   opcode plus the function code (if applicable). */
  final static int ADD = 15;     // R-format instructions
  final static int SUB = 16;
  final static int AND = 17;
  final static int ORR = 18;
  final static int NOT = 19;
  final static int TCP = 20;
  final static int SHL = 21;
  final static int SHR = 22;
  final static int RWD = 42;
  final static int WWD = 43;
  final static int JPR = 40;
  final static int JRL = 41;
  final static int HLT = 44;
  final static int ENI = 45;
  final static int DSI = 46;
  final static int ADI = 4;     // I-format instructions
  final static int ORI = 5;
  final static int LHI = 6;
  final static int LWD = 7;
  final static int SWD = 8;
  final static int BNE = 0;
  final static int BEQ = 1;
  final static int BGZ = 2;
  final static int BLZ = 3;
  final static int JMP = 9;    // J-format instructions
  final static int JAL = 10;
  
  final static int PC = Constants.PC;
	final static int GPR2 = Constants.GPR2;

	final static int DATA = Constants.DATA;
	final static int INSTRUCTION = Constants.INSTRUCTION;
	final static int NONE = Constants.NONE;
	final static int DECIMAL = Constants.DECIMAL;
	final static int HEX = Constants.HEX;

	final static int OPCODE_MASK =   0x0000000F;
	final static int FUNCTION_MASK = 0x0000003F;
	final static int REGISTER_MASK = 0x00000003;
	final static int IMM_MASK =      0x000000FF;
	final static int TARGET_MASK =   0x00000FFF;
	final static int FOUR_HIGH_BITS_MASK = 0x0000F000;

  final static String COLON = ": ";
  final static String COMMA = ", ";
  final static String COMMA$ = ", $";
	final static int YIELD_SIZE = 16;  // Number of iterations completed before
	                                   // passing control to another thread.
	private static StringBuffer tempBuff = new StringBuffer();
    // All purpose buffer used to prevent continual reallocation

	private Memory mem;
	private Registers regs;
  private Monitor monitor;
	private InstructionTrace instTrace;
	private MonitorInfo mi = new MonitorInfo();

	private int numberOfInst = 0;  // # of instr since the last HLT
	private int opcode = 0;        // Current instr's opcode
	private int function = 0;      // Current instr's function code
	private int rs = 0;            // Current instr's rs number
	private int rt = 0;            // Current instr's rt number
	private int rd = 0;            // Current instr's rd number
	private int imm = 0;           // Current instr's immediate value
	private int target = 0;        // Current instr's target address
  private int currentPC = 0;     // Holds the current pc value
	private boolean halt = false;  // Set to true when HLT is executed
	                               // Only used in 'step' mode
	private boolean firstCycle = false;  /* = true for the first cycle after
	  'start'/'step' has been pressed. Its false thereafter.  Used to make 
		sure that the Sim does not stop if the first instruction is a breakpoint. */




	public Sim(Registers regs, Memory mem, Monitor monitor, 
	           InstructionTrace instTrace)
	{
	  super("Sim");  // Name this thread Sim

    // Initialize simulator parts
    this.regs = regs;
		this.mem = mem;
		this.monitor = monitor;
		this.instTrace = instTrace;
	}  // End of Constructor


	
	
	/* This is the heart of the Simulator (looks pretty scarry, 
	   doesn't it.)  It continually loops but it is put to sleep 
		 when stop is pressed.  Only pressing 'start' or 'step' 
		 will wake it up again.  'mi.stepsize' must be greater 
		 than 0.  
			
		 Note that control will be passed to another thread every
		 YIELD_SIZE iterations.  This was implemented because the 
		 event thread under Win NT appears to have a low priority so 
	   button clicks were not registering for tens of seconds (the 
		 problem doesn't exist under Win 95.)  Implementing this 
		 "yielding" approach fixed the Win NT problem and actually 
		 improved execution time under Win 95.  */
 
	public void run()
	{
    int iterations = 0;
    int stepsize = 1;		
    while (true) {

		  if (mi.step) {                      // In 'step' mode
			  stepsize = mi.stepsize;
				iterations = 0;
        while (iterations < stepsize)  {
      		if (monitor.isStopPressed())  {
            monitor.waitForStart(mi);     // 'stop' was pressed
						firstCycle = true;
						iterations = stepsize;        // Break out of while loop
					}
          else {
					  if ((!firstCycle) && (mi.breakpointSet) 
                && (regs.readReg(PC) == mi.breakpoint))
       		    monitor.executionHalted(false);  // Presses 'stop' key
						else  {
      		    fetch();
      		    execute();
				  		iterations++;
						  if ((iterations == stepsize) && (!halt))
      				  monitor.executionHalted(false);  // Presses 'stop' key
							if ((iterations % YIELD_SIZE) == 0)
							  yield();       // Pass control to another thread
						}
					}
				}  // End of while(iterations < stepsize)
      }  // End of if(mi.step)


			else  {                              // In 'execute' mode
    		if (monitor.isStopPressed())  {
          monitor.waitForStart(mi);        // 'stop' was pressed
					firstCycle = true;
				}
  			else	{
				  if ((!firstCycle) && (mi.breakpointSet)
               						  && (regs.readReg(PC) == mi.breakpoint))
    		    monitor.executionHalted(false);     // Presses 'stop' key
  				else  {
    		    fetch();
  		      execute();
			  		iterations++;
						if ((iterations % YIELD_SIZE) == 0)
						  yield();       // Pass control to another thread
  				}
				}
		  }  // End of if(mi.step) else 
		}  // End of while(true)
	}  // End of run




  /* Fetch and decode the next instruction. The PC is incremented 
	   here and the instruction trace is updated. */
	private void fetch()
	{
		int instr;
		
		currentPC = regs.readReg(PC);
		firstCycle = false;
		
		// Fetch the instruction
		instr = mem.readMem(currentPC, INSTRUCTION);
		regs.writeReg(PC, currentPC + 1);  // Increment PC
		numberOfInst++;

		// Decode instruction.  Remember: before any binary operation 
		// is executed in Java, all operands are casted to integers.
		function = instr & FUNCTION_MASK;
		imm = (int) (byte) (instr & IMM_MASK);  // imm is a signed value
		target = instr & TARGET_MASK;
		rd = (instr >>= 6) & REGISTER_MASK;
		rt = (instr >>= 2) & REGISTER_MASK;
		rs = (instr >>= 2) & REGISTER_MASK;
		opcode = (instr >> 2) & OPCODE_MASK;
		
    if (opcode == 15)
		  opcode += function;
  }  // End of fetch




  // Execute the current instruction
	private void execute()
	{
    int addr = 0;
		int data = 0;

    halt = false;  // Only used in step mode
			
    // Prepare instruction trace string (if trace is on)
		if (mi.traceOn)  {
		  tempBuff.setLength(0);
		  if (instTrace.instNumberFormat != NONE)
			  tempBuff.append(numberOfInst).append(COLON);
      if (instTrace.pcFormat == DECIMAL)
  		  tempBuff.append(currentPC).append(COLON);
			else if (instTrace.pcFormat == HEX)
  		  tempBuff.append(Formatter.intToShortHexString(currentPC)).append(COLON);
		}

    switch (opcode)  {
		  
			case ADD:
			  regs.writeReg(rd, regs.readReg(rs) + regs.readReg(rt));
    		if (mi.traceOn)  {
				  tempBuff.append("ADD $").append(rd).append(COMMA$).append(rs)
					                                   .append(COMMA$).append(rt);
          instTrace.addInst(tempBuff.toString());
				}
				break;

      case ADI:
			  regs.writeReg(rt, regs.readReg(rs) + imm);
    		if (mi.traceOn)  {
				  tempBuff.append("ADI $").append(rt).append(COMMA$).append(rs)
					                                   .append(COMMA).append(imm);
          instTrace.addInst(tempBuff.toString());
				}
				break;

      case AND:
			  regs.writeReg(rd, regs.readReg(rs) & regs.readReg(rt));
    		if (mi.traceOn)  {
				  tempBuff.append("AND $").append(rd).append(COMMA$).append(rs)
					                                   .append(COMMA$).append(rt);
          instTrace.addInst(tempBuff.toString());
				}
				break;

      case BEQ:
			  if (regs.readReg(rs) == regs.readReg(rt))
			    regs.writeReg(PC, currentPC + 1 + imm);
    		if (mi.traceOn)  {
				  tempBuff.append("BEQ $").append(rs).append(COMMA$).append(rt)
					                                   .append(COMMA).append(imm);
          instTrace.addInst(tempBuff.toString());
				}
				break;

      case BGZ:
			  // The 'short' cast below sign-extends the register value.
			  if ((short) regs.readReg(rs) > 0)
			    regs.writeReg(PC, currentPC + 1 + imm);
    		if (mi.traceOn)  {
				  tempBuff.append("BGZ $").append(rs).append(COMMA).append(imm);
          instTrace.addInst(tempBuff.toString());
				}
				break;

      case BLZ:
			  // The 'short' cast below sign-extends the register value.
			  if ((short) regs.readReg(rs) < 0)
			    regs.writeReg(PC, currentPC + 1 + imm);
    		if (mi.traceOn)  {
				  tempBuff.append("BLZ $").append(rs).append(COMMA).append(imm);
          instTrace.addInst(tempBuff.toString());
				}
				break;

      case BNE:
			  if (regs.readReg(rs) != regs.readReg(rt))
			    regs.writeReg(PC, currentPC + 1 + imm);
    		if (mi.traceOn)  {
				  tempBuff.append("BNE $").append(rs).append(COMMA$).append(rt)
					                                   .append(COMMA).append(imm);
          instTrace.addInst(tempBuff.toString());
				}
				break;

      case DSI:
    		if (mi.traceOn)  {
				  tempBuff.append("DSI");
          instTrace.addInst(tempBuff.toString());
				}
				break;

      case ENI:
    		if (mi.traceOn)  {
				  tempBuff.append("ENI");
          instTrace.addInst(tempBuff.toString());
				}
				break;

      case HLT:
			  halt = true;
        numberOfInst = 0;
			  monitor.executionHalted(true);
    		if (mi.traceOn)  {
				  tempBuff.append("HLT");
          instTrace.addInst(tempBuff.toString());
  		    instTrace.addInst(" ");   // Insert blank line after Halt
				}
				break;

      case JAL:
			  target = ((currentPC + 1) & FOUR_HIGH_BITS_MASK) | target;
		    regs.writeReg(GPR2, currentPC + 1);
		    regs.writeReg(PC, target);
    		if (mi.traceOn)  {
					if (instTrace.pcFormat == DECIMAL)
  				  tempBuff.append("JAL ").append(target);
					else 
		  		  tempBuff.append("JAL ").append(Formatter.intToShortHexString(target));
          instTrace.addInst(tempBuff.toString());
				}
				break;

      case JMP:
			  target = ((currentPC + 1) & FOUR_HIGH_BITS_MASK) | target;
		    regs.writeReg(PC, target);
    		if (mi.traceOn)  {
					if (instTrace.pcFormat == DECIMAL)
  				  tempBuff.append("JMP ").append(target);
					else 
		  		  tempBuff.append("JMP ").append(Formatter.intToShortHexString(target));
          instTrace.addInst(tempBuff.toString());
				}
				break;

      case JPR:
		    regs.writeReg(PC, regs.readReg(rs));
    		if (mi.traceOn)  {
				  tempBuff.append("JPR $").append(rs);
          instTrace.addInst(tempBuff.toString());
				}
				break;

      case JRL:        // Note that JRL $2 does not work (by design)
		    regs.writeReg(GPR2, currentPC + 1);
		    regs.writeReg(PC, regs.readReg(rs));
    		if (mi.traceOn)  {
				  tempBuff.append("JRL $").append(rs);
          instTrace.addInst(tempBuff.toString());
				}
				break;

      case LHI:
			  regs.writeReg(rt, imm << 8);
    		if (mi.traceOn)  {
				  tempBuff.append("LHI $").append(rt).append(COMMA)
					                        .append(imm & 0x000000ff);
          instTrace.addInst(tempBuff.toString());
				}
				break;

      case LWD:
			  regs.writeReg(rt, mem.readMem(regs.readReg(rs) + imm, DATA));
    		if (mi.traceOn)  {
				  tempBuff.append("LWD $").append(rt).append(COMMA$).append(rs)
					                                   .append(COMMA).append(imm);
          instTrace.addInst(tempBuff.toString());
				}
				break;

      case NOT:
			  regs.writeReg(rd, ~regs.readReg(rs));
    		if (mi.traceOn)  {
				  tempBuff.append("NOT $").append(rd).append(COMMA$).append(rs);
          instTrace.addInst(tempBuff.toString());
				}
				break;

      case ORI:
			  // ORI is the only instruction for which the immediate value is 
				// not signed extended.
				imm = imm & IMM_MASK;
			  regs.writeReg(rt, regs.readReg(rs) | imm);
    		if (mi.traceOn)  {
				  tempBuff.append("ORI $").append(rt).append(COMMA$).append(rs)
					                                   .append(COMMA).append(imm);
          instTrace.addInst(tempBuff.toString());
				}
				break;

      case ORR:
			  regs.writeReg(rd, regs.readReg(rs) | regs.readReg(rt));
    		if (mi.traceOn)  {
				  tempBuff.append("ORR $").append(rd).append(COMMA$).append(rs)
					                                   .append(COMMA$).append(rt);
          instTrace.addInst(tempBuff.toString());
				}
				break;

      case RWD:
			  // Send the trace string to the trace window so the user can 
				// see that data is expected.
    		if (mi.traceOn)  {
				  tempBuff.append("RWD $").append(rd);
          instTrace.addInst(tempBuff.toString());
				}
			  if (monitor.requestInputData(mi))   // If 'stop' was not pressed
				  regs.writeReg(rd, mi.inputData);
				else  {         // 'stop' was pressed or file error so cancel RWD
          if (mi.traceOn)
					  instTrace.removeLastInst();
					numberOfInst--;
					regs.writeReg(PC, currentPC);  // Restore the PC
					halt = true;
				}
				break;

      case SHL:
			  regs.writeReg(rd, regs.readReg(rs) << 1);
    		if (mi.traceOn)  {
				  tempBuff.append("SHL $").append(rd).append(COMMA$).append(rs);
          instTrace.addInst(tempBuff.toString());
				}
				break;

      case SHR:
			  // The 'short' cast below sign-extends the register value.
			  regs.writeReg(rd, (short) regs.readReg(rs) >> 1);
    		if (mi.traceOn)  {
				  tempBuff.append("SHR $").append(rd).append(COMMA$).append(rs);
          instTrace.addInst(tempBuff.toString());
				}
				break;

			case SUB:
			  regs.writeReg(rd, regs.readReg(rs) - regs.readReg(rt));
    		if (mi.traceOn)  {
				  tempBuff.append("SUB $").append(rd).append(COMMA$).append(rs)
					                                   .append(COMMA$).append(rt);
          instTrace.addInst(tempBuff.toString());
				}
				break;

      case SWD:
			  mem.writeMem(regs.readReg(rs) + imm, regs.readReg(rt), DATA);
    		if (mi.traceOn)  {
				  tempBuff.append("SWD $").append(rt).append(COMMA$).append(rs)
					                                   .append(COMMA).append(imm);
          instTrace.addInst(tempBuff.toString());
				}
				break;

      case TCP:
			  regs.writeReg(rd, ~regs.readReg(rs) + 1);
    		if (mi.traceOn)  {
				  tempBuff.append("TCP $").append(rd).append(COMMA$).append(rs);
          instTrace.addInst(tempBuff.toString());
				}
				break;

      case WWD:
			  // Send the trace string to the trace window so the user can 
				// see that data has been sent.
    		if (mi.traceOn)  {
				  tempBuff.append("WWD $").append(rs);
          instTrace.addInst(tempBuff.toString());
				}
        // If 'stop' was pressed or file error
			  if (!monitor.sendOutputData(regs.readReg(rs))) { 
          if (mi.traceOn)
					  instTrace.removeLastInst();
					numberOfInst--;
					regs.writeReg(PC, currentPC);  // Restore the PC
					halt = true;
				}
				break;

      default:
    		if (mi.traceOn)  {
				  tempBuff.append("Invalid Opcode");
          instTrace.addInst(tempBuff.toString());
				}
			  break;

    } // End of switch statement
			  
	}  // End of execute
	
}  // End of Sim class

