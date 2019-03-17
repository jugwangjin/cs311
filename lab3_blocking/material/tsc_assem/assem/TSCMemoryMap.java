/*
© 2000, Performance Evaluation Laboratory, Brigham Young University. Reproduction of all or part of this work is permitted for non-profit educational
or research use provided this copyright notice remains intact. All other rights reserved. 
*/

import java.io.*;
import java.awt.Dimension;
import java.util.Vector;

/* This class prints a memory map of a TSC program by analyzing
   the contents of a TSC binary file.  It can be executed directly
	 from a command line or called from another program.  */

class TSCMemoryMap
{
  final static String NEWLINE = System.getProperty("line.separator");


  /* Prints the memory map of 'file'.  Also indicates if any of the 
	   segments overlap.  'file' should not be null.  */
	public static void showMemMap(File file)
	{
    RandomAccessFile fileStream;
		Vector segList;

    try  {
  		fileStream = new RandomAccessFile(file, "r");
		} catch (IOException e)  {
			  System.out.println("I/O Exception occurred while opening " 
				                                         + e.getMessage());
				return;
		}

    try  {
      int numSegments = 0;
			int segAddress = 0;
			int numShorts = 0;
			short aShort = 0;

      // Get the number of segments
      numSegments = fileStream.readShort() & 0x0000ffff;
			segList = new Vector(numSegments, 1);
			
      // Read in the segment info
			for (int seg = 0; seg < numSegments; seg++)  {
			  System.out.println("Segment " + (seg + 1));
        segAddress = fileStream.readShort() & 0x0000ffff;
        numShorts = fileStream.readShort() & 0x0000ffff;
				segList.add(new Segment(segAddress, segAddress + numShorts -1));

        // Print each short in the segment
        for (int i = 0; i < numShorts; i++)  {
          aShort = fileStream.readShort();
				  System.out.println("0x" + Tools.shortToHexString((short) segAddress++)
				                             + ":  0x" + Tools.shortToHexString(aShort));
        }
			  System.out.print(NEWLINE);
			}
		
		} catch (IOException e)  {
	      System.out.println("Error: could not read " + file.getAbsolutePath());
				return;
		} finally  {
		    try  {
				  fileStream.close();
				} catch (IOException ioe)  {
  			    System.out.println("Could not close " + file.getAbsolutePath());
						return;
				}
		}

		// Check to see if any of the segments overlap.
		for (int seg1 = 0; seg1 < segList.size() - 1; seg1++)  {
			for (int seg2 = seg1 + 1; seg2 < segList.size(); seg2++)  {
			  if (((Segment) segList.get(seg1)).isOverlapping(
				                    (Segment) segList.get(seg2)))  {
				  System.out.println("Warning> Segments " + (seg1 + 1) 
					                + " and " + (seg2 + 1) + " overlap");
				}
			}
		}
		
  }  // End of showMemMap




  /* Used by main when parsing the command line */
  private static void usageError()  {
	  System.out.println("Usage Error: java -classpath <path> TSCMemoryMap <filename>");
	}  // End of usageError




  /*  If main receives no arguements, it starts the GUI.  */
  public static void main(String args[])
  {
    // If there are no arguements then start the GUI
		if (args.length == 0)  {
		  System.out.println("Starting the TSC Assembler GUI");
      TSCAssemblerGUI gui = new TSCAssemblerGUI(new TSCAssembler());

      // Position the gui away from the edge of the screen
			Dimension screenSize = gui.getToolkit().getScreenSize();
			Dimension guiSize = gui.getSize();
			gui.setLocation((screenSize.width - guiSize.width) / 4,
			                (screenSize.height - guiSize.height) / 4);

			gui.show();
		}


		else  {
      // The interface is being run in command line mode

      boolean foundFile = false;
			
	    //Parse the command line options and assemble the file
		  for (int x = 0; x < args.length; x++)  {

        if (args[x].charAt(0) == '-')  {
				  // It's an option
				  if (args[x].length() == 1)  {
					  usageError();
						System.exit(1);
					}
					else  {
					  // Put options here... (see the example option below)
/*
						// The show symbol table option.
            if (args[x].substring(1).compareToIgnoreCase("st") == 0)  {
						  tscAssembler.showSymbolTable = true;
							continue;  // Go to the next iteration of the loop
					  }
*/
					  // The option is invalid
						usageError();
						System.exit(1);
					}	
				}
				
				else  {
  				// It's the filename
				  foundFile = true;
					if (x < args.length - 1)  {
					  usageError();
						System.out.println("  All options must be placed before the filename");
						System.exit(1);
					}
					if (!FileTools.getExtension(args[x]).equalsIgnoreCase("bin"))  {
					  System.out.print("TSCMemoryMap Error:  " + args[x]);
						System.out.println(" could not be opened.");
						System.out.print("  Make sure the file exists and has a .bin");
						System.out.println(" extension");
				  }
					else
					  showMemMap(new File(args[x]));
				}
			}

      if (!foundFile)  {
				usageError();
				System.exit(1);
		  }
		}
		
  } // End of main

}  // End of TSCMemoryMap class
