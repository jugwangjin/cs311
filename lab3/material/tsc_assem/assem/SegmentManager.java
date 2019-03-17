/*
© 2000, Performance Evaluation Laboratory, Brigham Young University. Reproduction of all or part of this work is permitted for non-profit educational
or research use provided this copyright notice remains intact. All other rights reserved. 
*/

import java.util.Vector;

/*  This class keeps track of the beginning and end of all program 
    segments.  This info is used when creating the .bin file.  */

class SegmentManager
{
	private final static int ORG = Assembler.ORG;
	private final static int END = Assembler.END;
	private final static int EQU = Assembler.EQU;
	private final static int BSS = Assembler.BSS;
	private final static int BSC = Assembler.BSC;
	private final static int LAD = Assembler.LAD;
	private final static int MAXMEMSIZE = Assembler.MAXMEMSIZE;

	private int locCounter;  // Always points to the next available mem location
	private Vector segmentList;
	private boolean newSegment;



	public SegmentManager()
	{
		segmentList = new Vector(1, 1);
    locCounter = 0;
		newSegment = true;    // Ensures that a new segment will be created for the 
		                      // first instruction.
	}  //  End of constructor
	
	
	
	
	/* This method takes a code line as a parameter and updates 
	   the location counter based on the mnemonic in the line.  
		 
		 The beginning and ending locations of each program segment 
		 are also recorded.  This info is used when creating the 
		 .bin file.  It is assumed that a new segment can only 
		 be created when an ORG is encountered (with the exception 
		 of the initial segment.)  The order of the segments in the 
		 segment list should correspond with the order of the 
		 code in the code list.  */
	public boolean updateLocCounter(CodeLine cl)
	{
		int oldLocCounter = locCounter;  // Save the starting location
	  cl.address = locCounter;

		// Increment the location counter
		if (!cl.directive && !cl.pseudoInstr)  {
			locCounter++;
		}

		else  {
			switch (cl.mnemonic)  {
			
				case ORG:
				  cl.address = -1;         // ORG doesn't actually have an address
				  locCounter = cl.operandValue;
					newSegment = true;       // Create a new segment
					break;

				case BSC:
				  locCounter += cl.operandList.size();
					break;

				case BSS:
				  locCounter += cl.operandValue;
					break;

				// The state of the memory manager does not change for EQU or END
				case END:
				case EQU:
				  cl.address = -1;      // END and EQU do not actually have addresses
					return true;

        case LAD:
          locCounter += 2;
          break;

				default:
					System.out.println("Segment Manager internal error: no case for mnemonic = "
					  + cl.mnemonic);
			}
		}					

		if (locCounter > MAXMEMSIZE + 1)  // Memory bounds exceeded
			return false;

		if (cl.mnemonic != ORG)  {
			// Update segment counter
			if (newSegment)  {        // Create a new segment
			  segmentList.add(new Segment(oldLocCounter, locCounter - 1));
				newSegment = false;
			}
			else  {                   // Use the current segment
				((Segment) segmentList.lastElement()).end = locCounter - 1;
			}
		}
		
		return true;

	}  // End of updateLocCounter




  public int getLocCounter()
	{
		return locCounter;
	}  // End of getLocCounter




	public Vector getSegmentList()
	{
		return (Vector) segmentList.clone();
	}  // End of getSegmentList

}  // End of SegmentManager class

