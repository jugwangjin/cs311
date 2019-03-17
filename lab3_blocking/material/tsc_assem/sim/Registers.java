/*
© 2000, Performance Evaluation Laboratory, Brigham Young University. Reproduction of all or part of this work is permitted for non-profit educational
or research use provided this copyright notice remains intact. All other rights reserved. 
*/

/* The Registers class contains the values for each register
   in TSC.  Updating any of the registers also updates the 
	 display. */

public class Registers
{
  final static int NUMREGS = Constants.NUMREGS;
	final static int PC = Constants.PC;
	final static int GPR0 = Constants.GPR0;
	final static int GPR1 = Constants.GPR1;
	final static int GPR2 = Constants.GPR2;
	final static int GPR3 = Constants.GPR3;
	final static int HEX = Constants.HEX;
	final static int DECIMAL = Constants.DECIMAL;
	final static int SIGNED_DECIMAL = Constants.SIGNED_DECIMAL;
	final static int BINARY = Constants.BINARY;
	
  private Display display;
	
  // The register values
	private int regs[] = new int[NUMREGS];
  // The string representations of regs[]
	private String regStrings[] = new String[NUMREGS];
  // The display format for each register
	private int regFormat[] = new int[NUMREGS];
	



  public Registers()
	{
	  int i = 0;
		
    System.out.println("Initializing the registers...");

		for (i = 0; i < NUMREGS; i++)  {
		  regs[i] = 0;
			regStrings[i] = "0x0000";
			regFormat[i] = HEX;
		}
	}  // End of constructor
	
	

	
	/* Called by Display to inform Registers where to send the
	   register values */
	public void setDisplay(Display display)
	{
	  this.display = display;
	}  // End of setDisplay




  /* Returns the value of regs[regNumber]. 'regNumber' must be 
	   less than NUMREGS.  Note that the value returned will never 
		 be greater than the largest value a short can hold because 
		 the high 16 bits of any number are zeroed out before the 
		 number is written to a register. */
	public int readReg(int regNumber)
	{
 		return regs[regNumber];
	}  // End of readReg




  /* Updates regs[regNumber] to 'newValue' and then updates the display.
	   The high 16 bits of 'newValue' will always be zeroed out before it 
		 is stored in the register.  'regNumber' must be less than NUMREGS. */
	public void writeReg(int regNumber, int newValue)
	{
		regs[regNumber] = newValue & 0x0000ffff;  // Update register
		updateRegString(regNumber);
	}  // End of writeReg

	
	
	
	/* Zero all registers	*/
	public void clearRegs()
	{
	  for (int i = 0; i < NUMREGS; i++)
		  writeReg(i, 0);
	}  // End of clearRegs




  /* Changes the display format of 'regNumber' to 'newFormat'. 
	   'regNumber' must be less than NUMREGS.  'newFormat' must 
		 be a valid format.  */
  public void updateRegFormat(int regNumber, int newFormat)
	{
    regFormat[regNumber] = newFormat;
		updateRegString(regNumber);
	}  // End of updateRegFormat
	



  /* Updates the string representation for 'regNumber. 
	   'regNumber' must be less than NUMREGS. */
  private void updateRegString(int regNumber)
	{
    switch (regFormat[regNumber])  {
      case HEX:  
        regStrings[regNumber] = Formatter.intToShortHexString(regs[regNumber]);
				break;
			
			case DECIMAL:
        regStrings[regNumber] = "" + regs[regNumber];
				break;

			case SIGNED_DECIMAL:
			  if (regNumber == PC)
          regStrings[regNumber] = "" + regs[regNumber];
				else
          regStrings[regNumber] = "" + (int) (short) regs[regNumber];
			  break;
			
			case BINARY:
        regStrings[regNumber] = Formatter.intToShortBinaryString(regs[regNumber]);
				break;

      default:
			  System.out.println("Error in Registers.updateRegFormat(): Invalid format");
			  break;
				
		}  // End of switch statement

	  // Update display.  Note that setText() is thread safe so it does not
	  // have to be inserted into the event thread.
		switch (regNumber)  {
		  case PC:
	      display.tscConsole.pcDisplay.setText(regStrings[PC]);
				break;
			case GPR0:
	      display.tscConsole.gpr0Display.setText(regStrings[GPR0]);
				break;
			case GPR1:
	      display.tscConsole.gpr1Display.setText(regStrings[GPR1]);
				break;
			case GPR2:
	      display.tscConsole.gpr2Display.setText(regStrings[GPR2]);
				break;
			case GPR3:
	      display.tscConsole.gpr3Display.setText(regStrings[GPR3]);
			  break;
      default:
			  System.out.println("Error in Registers.updateRegFormat(): Invalid register");
			  break;

		}  // End of switch statement
				
	}  // End of updateRegString
	
}  // End of Registers class

