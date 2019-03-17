/*
© 2000, Performance Evaluation Laboratory, Brigham Young University. Reproduction of all or part of this work is permitted for non-profit educational
or research use provided this copyright notice remains intact. All other rights reserved. 
*/

import java.io.PrintStream;


/* The TSC Simulator for CS 143 at BYU.
   Written by Jeff Penfold during Summer Term 1999.
	
   The TSC Simulator consists of two main threads - the 
	 display and the simulator.  They communicate with each 
	 other through a special purpose monitor object. A 
	 register object, a memory object, and an instruction 
	 trace object are used to keep the state of the TSC 
	 machine.
	
   This class is used to initialize and start the TSC 
   Simulator.  It is then discarded by the runtime 
	 environment. */

public class TSCSimulator
{

  public static void main(String args[]) 
  {
		// Initialize the error output console
		PrintStream errorConsole = new PrintStream(new ConsoleOutputStream(
		                                      "Another #$@%&*! TSC Error"));
		System.setErr(errorConsole);

    // Set up memory, the registers, and the display
	  Registers registers = new Registers();
	  Memory memory = new Memory();
    Monitor monitor = new Monitor();
	  InstructionTrace instTrace = new InstructionTrace();
    Display display = new Display(registers, memory, monitor, instTrace);
		
    // Make the GUI visible
    display.setTitle("TSC Simulator");
    display.pack();
		display.setLocation(20, 20);  // Move gui away from the corner
    display.show();

    // Start the simulator		
    System.out.println("Starting the Simulator...");
		Sim sim = new Sim(registers, memory, monitor, instTrace);
		sim.start();

    System.setOut(errorConsole);
		
	}  // End of main

}  // End of TSCSimulator class

