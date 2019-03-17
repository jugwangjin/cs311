/*
© 2000, Performance Evaluation Laboratory, Brigham Young University. Reproduction of all or part of this work is permitted for non-profit educational
or research use provided this copyright notice remains intact. All other rights reserved. 
*/

/*  This class is passed by Sim into the monitor so that 
    the monitor's variables can be stored in it and returned
		to Sim.  It is the only way I can think of to return 
		multiple variables from the monitor. */
class MonitorInfo
{
	boolean step = false;
  int stepsize = 1;
  boolean breakpointSet = false;
	int breakpoint = 0;
  int inputData = 0;
	boolean traceOn = true;
}  // End of MonitorInfo class

