/*
© 2000, Performance Evaluation Laboratory, Brigham Young University. Reproduction of all or part of this work is permitted for non-profit educational
or research use provided this copyright notice remains intact. All other rights reserved. 
*/

import javax.swing.DefaultListModel;
import javax.swing.SwingUtilities;

/* The list (actually, its a vector) that holds the 
   instruction trace. */

public class InstructionTrace extends DefaultListModel
{
 	final static int NONE = Constants.NONE;
	
	// Max. number of instructions allowed in the list.  This prevents 
	// a memory overflow (hopefully) if a long program is running.
	final static int MAX_INSTRUCTIONS = 10000;
	final static int REMOVE_INST_BLOCK_SIZE	= 1000;
		
	// Used to ensure that the newest instruction line is visible in the
	// trace window.  An instance is created here to avoid creating a new 
	// instance every time a trace line is added or removed.
	private EnsreVis ensureTraceIsVisible = new EnsreVis();

  // Indicates if the instruction number is shown on the line.
  public int instNumberFormat = NONE;

  /* Indicates if the PC is shown on the line and what format it's in.
	   The format is the same as the address value format.  'pcFormat' 
	   should ONLY be changed through the setPCFormat method in the 
		 DebugConsole class.  This ensures the format is based on the 
		 status of the Show PC checkbox. */
	public int pcFormat;

	private Display display;




	/* Called by Display to inform InstructionTrace where to send 
	   any display signals. */
	public void setDisplay(Display display)
	{
	  this.display = display;
		pcFormat = display.mem.memAddressFormat;
	}  // End of setDisplay


	
	
	/* Removes the last instruction from the trace. */
  public synchronized void removeLastInst()
	{
	  removeElementAt(getSize() - 1);
		ensureTraceIsVisible.setLocation(getSize() - 1);
		SwingUtilities.invokeLater(ensureTraceIsVisible);
	}  // End of removeLastInst




  /* Adds an instruction line to the list and makes it visible in the 
	   window.  If the number of instructions in the list exceeds the 
		 maximum amount allowed, remove the first REMOVE_INST_BLOCK_SIZE 
		 instructions.  Hopefully this will prevent memory overflow (the 
		 Java heap doesn't seem to want to exceed 64 MB.)  
		 
		 The instructions are removed by inserting the removeRange() 
		 method into the event dispatching thread.  This insertion is done
		 because in some instances, the paint() method in the instruction
		 trace window can be interrupted by the removeRange() method.
		 Then when painting continues, calls to getElementAt() by the
		 paint() method references an invalid index (because the list
		 wasn't as long as it used to be.)  Note that using invokeLater()
		 to insert removeRange() into the event dispatching thread, for
		 some reason, cleared the instruction trace window before calling
		 removeRange() and then threw an exception because there were no
		 instructions to remove.  Because invokeAndWait() is used, this 
		 method, addInst(), SHOULD NEVER BE CALLED FROM the event 
		 dispatching thread.  Currently, it is only called from the Sim
		 thread.  */
	public synchronized void addInst(String instr)
	{
	  if (getSize() > MAX_INSTRUCTIONS)  {
      try  {
			  SwingUtilities.invokeAndWait(new RmvBlck());
      } catch (Exception e) {
			    System.out.println("Error in InstructionTrace.addInst()");
					System.out.println("  " + e.getMessage());
      }
		}
		addElement(instr);
		ensureTraceIsVisible.setLocation(getSize() - 1);
		SwingUtilities.invokeLater(ensureTraceIsVisible);
	}  // End of addInst




	/* This nested class removes the first REMOVE_INST_BLOCK_SIZE 
	   instructions from the instruction list.  */
	private class RmvBlck implements Runnable
	{
	  public void run()
		{
		  if (getSize() > REMOVE_INST_BLOCK_SIZE)    // Just in case :)
	      removeRange(0, REMOVE_INST_BLOCK_SIZE - 1);
		}
	}  // End of RmvBlck class




  /* This nested class is used by addInst() to make the instruction 
	   that was just added to the list visible in the trace window. */
	private class EnsreVis implements Runnable
	{
    private int location;

		public void setLocation(int location)
		{
		  this.location = location;
		}

	  public void run()
		{
      display.debugConsole.traceDisplay.ensureIndexIsVisible(location);
  	}
	}  // End of EnsreVis class

}  // End of InstructionTrace class


