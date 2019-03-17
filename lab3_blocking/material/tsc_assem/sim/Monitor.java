/*
© 2000, Performance Evaluation Laboratory, Brigham Young University. Reproduction of all or part of this work is permitted for non-profit educational
or research use provided this copyright notice remains intact. All other rights reserved. 
*/


/* The Monitor is used to send data and signals back and forth 
   between the GUI thread and the SIM thread.  The monitor is also 
	 used to put the Sim to sleep (using wait()) when it is not needed 
	 and to wake it up when it is needed.
	
	 The Sim calls isStopPressed at the beginning of each execution 
	 cycle to check the 'execute' var.  The GUI calls stopPressed when 
	 the stop button has been pressed to set 'execute' to false. When
	 isStopPressed returns true the Sim calls waitForStart and is put to 
	 sleep.  When 'start' or 'step' is pressed, the GUI calls 
	 startPressed and passes in all its data.  This also wakes up the Sim.
	 The Sim takes the data and starts the cycle over again.
	
	 If the Sim executes the HLT instruction before 'stop' is pressed, 
	 it calls executionHalted which simply clicks the appropriate stop 
	 button on the GUI (if an input file was used, it is reset.)  
	 Pressing the stop button causes stopPressed to be called and the 
	 pattern described in the above paragraph is followed.
	
	 executionHalted is also called when a breakpoint is reached, when 
	 stepSize instructions have been executed, or when an I/O error 
	 occurs (the input file is NOT reset for any of these).  
	
	 When the Sim executes a RWD instruction, requestInputData is called 
	 to inform the GUI it needs data.  This also puts the Sim to sleep 
	 while it is waiting for the data.  The GUI then gets the data (from 
	 the console or from a file) and passes it into sendInputData.  This 
	 wakes up the Sim and execution continues.  The RWD instruction is 
	 cancelled if 'stop' is pressed before 'send data' is pressed or if 
	 there is an I/O error. cancelInputData is called to perfom the 
	 cancellation. If 'stop' is pressed while input is coming from a file, 
	 then the RWD instruction is allowed to finish executing i.e. the 
	 console only calls cancelInputData when the input source is NOT a 
	 file.
	
	 When the Sim executes a WWD instruction, sendOutputData is called 
	 to give the data to the GUI.  This also puts the Sim to sleep while 
	 it is waiting for an ack.  The GUI then acks the data by calling 
	 ackOutputData.  This wakes up the Sim and execution continues.  The 
	 WWD instruction is cancelled if 'stop' is pressed before 'ack data' 
	 or if there is an I/O error.  cancelOutputAck is called to perform 
	 the cancellation.  If 'stop' is pressed while output is being written 
	 to a file, then the WWD instruction is allowed to finish executing 
	 i.e. the console only calls cancelOutputAck when one of the output 
	 destinations is the console lights and auto ack is off.  */


import javax.swing.SwingUtilities;


class Monitor
{
	final static int INPUT = Constants.INPUT;
	final static int OUTPUT = Constants.OUTPUT;
	
  private Display display;

  // These variables represent the state of the monitor
	private boolean execute = false;  // =true when 'start' or 'step' is pressed
	private boolean step = false;     // =true when 'step' is pressed
  private int stepsize = 1;
  private boolean breakpointSet = false;
	private int breakpoint = -1;
  private int inputData = 0;
	private int outputData = 0;
  private boolean traceOn = true;
	private boolean inputAvailable = false;
	private boolean outputAcked = false;
  private boolean inputDataCancelled = false;
  private boolean waitingForInputData = false;
  private boolean outputAckCancelled = false;
  private boolean waitingForOutputAck	= false;


	
	/* Called by the display to inform Monitor where to send the
	   Sims data and signals. */
	public void setDisplay(Display display)
	{
	  this.display = display;
	}  // End of setDisplay




  /* Used by the Sim to wait for the start or step button
	   to be pressed.  The Sim sleeps until startPressed() is
		 called by the GUI.  Most of the data from the GUI is 
		 given to the Sim here.*/
  public synchronized void waitForStart(MonitorInfo mi)
  {
	  while (!execute)  {
      try {
        wait();
      } catch (InterruptedException e) { }
		}
		mi.step = this.step;
		mi.stepsize = this.stepsize;
		mi.breakpointSet = this.breakpointSet;
		mi.breakpoint = this.breakpoint;
		mi.traceOn = this.traceOn;
  }  // End of waitForExecute




  /* Used by the GUI to tell the SIM that the start or step button
	   has been pressed. All the signals that the Sim needs to operate 
		 are set here. */
	public synchronized void startPressed(boolean step, int stepsize,
             boolean breakpointSet, int breakpoint, boolean traceOn)
	{
	  this.step = step;
		this.stepsize = stepsize;
		this.breakpointSet = breakpointSet;
		this.breakpoint = breakpoint;
		this.traceOn = traceOn;
		execute = true;
		notifyAll();  // Wake up Sim
	}  // End of startPressed
	



  /* Used by the Sim to check to see if the stop button has 
	   been pressed.  Note: do NOT change 'step' to false.  It may 
		 mess up the executionHalted() method. */
	public synchronized boolean isStopPressed()
  {
	  if (!execute)  return true;
		else  return false;
	}  // End of isStopPressed
		 



  /* Used by the GUI to tell the SIM that the stop button
	   has been pressed. */
  public synchronized void stopPressed()
	{
	  execute = false;
	}  // End of stopPressed
	

	

  /* Used by the Sim to request and receive input data 
	   from the I/O console i.e. a RWD was executed.  The Sim 
		 is put to sleep until the data is ready to receive or 
		 'stop' is pressed.  Returns true if stop is not pressed 
     before the data is sent. */
	public synchronized boolean requestInputData(MonitorInfo mi)
	{
    // Reset state
    waitingForInputData = true;
    inputAvailable = false;
		inputDataCancelled = false;

    // Insert class into the event thread to request input data
    SwingUtilities.invokeLater(new CallGUI(INPUT));

    // Sleep until input data is available
	  while (!inputAvailable)  {
      try {
        wait();
      } catch (InterruptedException e) { }
		}
  
	  // Reset state
    waitingForInputData = false;
  
    if (inputDataCancelled)  {
		  // Input was from the console but 'stop' was pressed first
			// or there was a file error
		  mi.inputData = -1;  // Set to -1 for debugging
			return false;
		}
		else  {                           // Get the input data
		  mi.inputData = this.inputData;
		  return true;
		}
  } // End of requestInputData




  /* Used by the GUI to send the input data to the Sim. */
	public synchronized void sendInputData(int inputData)
	{
	  this.inputData = inputData;
		this.inputAvailable = true;
		notifyAll();  // Wake up Sim
	}  // End of sendInputData




  /* Called by the GUI when 'stop' is pressed and the console is the 
	   input source.  The GUI checks to see if the Sim is waiting for 
		 input data.  If it is, then it cancels the data and wakes up 
		 the Sim.  Also called when there is a file error. */ 
  public synchronized void cancelInputData()
	{
	  if (waitingForInputData)  {
		  inputAvailable = true;
			inputDataCancelled = true;
			notifyAll();
		}
	}  // End of cancelInputData




  /* Used by the Sim to send data to and receive an ack from 
	   the I/O console.  The Sim is put to sleep until the 
		 data is acked or 'stop' is pressed.  Returns false if stop 
		 is pressed before the data is acked. */
	public synchronized boolean sendOutputData(int outputData)
	{
	  // Reset state
    waitingForOutputAck = true;
    outputAcked = false;
		outputAckCancelled = false;
    this.outputData = outputData;
		
    // Insert class into the event thread to request ack for output data
    SwingUtilities.invokeLater(new CallGUI(OUTPUT));

	  while (!outputAcked)  {
      try {
        wait();
      } catch (InterruptedException e) { }
		}

	  // Reset state
    waitingForOutputAck = false;

    if (outputAckCancelled)  
		  return false;
		else
	    return true;
  } // End of sendOutputData




  /* Used by the GUI to ack output data from the Sim. */
	public synchronized void ackOutputData()
	{
	  outputAcked = true;
		notifyAll();
	}  // End of ackOutputData




  /* Called by the GUI when 'stop' is pressed and the console is the 
	   output dest.  The GUI checks to see if the Sim is waiting for 
		 an output ack.  If it is, then it cancels the ack and wakes up 
		 the Sim.  Also called when there is an output file error. */ 
  public synchronized void cancelOutputAck()
	{
	  if (waitingForOutputAck)  {
		  outputAcked = true;
			outputAckCancelled = true;
			notifyAll();
    }
	}  // End of cancelOutputAck




  /* Called when one of the following has ocurred:
	   1) HLT has been executed (so I/O files are reset),
		 2) the breakpoint has been reached,
		 3) 'stepsize' instructions have been executed,
		 4) a file I/O error was generated.
		 Execution is stopped by simply pressing the stop button.
		 
  	 Note that this method makes use of the CallGUI class below by 
		 overriding its run method.  This is done so that a new class 
		 does not need to be written. */
	public synchronized void executionHalted(final boolean hltExecuted)
	{
	  execute = false;
    SwingUtilities.invokeLater(new CallGUI(OUTPUT)
  		{
  	    public void run()
		    { 
					if (hltExecuted)  {
  					display.ioConsole.resetInputFile();
  					display.ioConsole.resetOutputFile();
					}
					if (step)
			      display.debugConsole.stepButton.doClick();
					else
					  display.tscConsole.startStopButton.doClick();
  		  }
		  }  // End of revamped CallGUI
		);
	}  // End of executionHalted




  /* This nested class is used by requestInputData() and 
	   sendOutputData() to request I/O from the I/O console.
		 It is also used by executionHalted(). */
	private class CallGUI implements Runnable
	{
    private int ioType;
		
		public CallGUI(int ioType)
		{
		  this.ioType = ioType;
		}

	  public void run()
		{
			if (ioType == INPUT)
  			display.ioConsole.sendData();
			else if (ioType == OUTPUT)
        display.ioConsole.receiveData(outputData);
  	}
	}  // End of RequestIO class
    
}  // End of Monitor class

