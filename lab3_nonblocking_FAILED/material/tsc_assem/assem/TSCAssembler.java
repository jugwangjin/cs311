/*
© 2000, Performance Evaluation Laboratory, Brigham Young University. Reproduction of all or part of this work is permitted for non-profit educational
or research use provided this copyright notice remains intact. All other rights reserved. 
*/

import java.io.*;
import java.awt.Dimension;


/* The TSC Assembler Interface for CS 143 at BYU.
	 Written by Jeff Penfold during June 1999.
	
	
   The assembler can be executed directly from the command line 
	 using this interface.  For example, typing
	     java TSCAssembler test5.tsc
	 on the command line will assemble test5.tsc (all code files 
	 must have a .tsc extension) and put the binary code in 
	 test5.bin.
	
   If this interface is run with no arguements, an assembler
	 GUI is started.

	 This interface can also be used by other programs to assemble
	 a tsc code file.	The calling program can specify the code 
	 file, set the output stream, set option flags, and then 
	 execute the assembler by calling go().   

	 The assembler options are:
		 -st      show symbol table
		 -seg     show segment info
		 -debug   print debug info in a .debug file

*/
	
class TSCAssembler
{
	private File codeFile;

  // Option flags
	public boolean showSymbolTable = false;  
	  // If true, the symbol table is printed after the file is assembled.
	public boolean showSegmentInfo = false;  
	  // If true, the segment info is printed after the file is assembled.
	public boolean debugMode = false;
	  // If true, the assembler outputs all instruction info into a .debug file.
		// This option is hidden from the casual user.




  public TSCAssembler()
	{
	  codeFile = null;
  } // End of constructor




	public TSCAssembler(String filename)
	{
    if (!setCodeFile(new File(filename)))  {
		  System.out.println("TSC Assembler Error:  " + filename 
			                            + " could not be opened.");
			System.out.println("  Make sure the file exists and has a .tsc extension");
		}
	}  // End of constructor

	
	

  public TSCAssembler(String filename, PrintStream newOut)
	{
    if (!setOutputStream(newOut))
		  System.out.println("tSC Assembler Error:  Could not change output stream.");
		
    if (!setCodeFile(new File(filename)))  {
		  System.out.println("TSC Assembler Error:  " + filename 
			                             + " could not be opened.");
			System.out.println("  Make sure the file exists and has a .tsc extension");
		}

	}  // End of constructor

	
	
	
  /* Set the code file.  Returns true if the file exists, has
	   a .tsc extension, and is readable.  Otherwise false is 
		 returned. */
	public boolean setCodeFile(File file)
	{
	  if (file != null)  {
		  String filename = file.getAbsolutePath();
		  if ((filename != null) && (filename.length() != 0))
			  if (FileTools.getExtension(filename).equalsIgnoreCase("tsc"))
					if (file.canRead())  {
					  codeFile = file;
					  return true;
					}
		}
	  return false;
	}  // End of setCodeFile




  /* Sets standard out and standard error output streams 
	   to 'newOut'.  Returns true if the stream was 
	   successfully set, otherwise false is returned.  */
	public boolean setOutputStream(PrintStream newOut)
	{
	  if (newOut != null)  {
      System.setOut(newOut);
			System.setErr(newOut);
			return true;
		}
		else  return false;
	}  // End of setOutputStream




  /* Call the assembler.  */
	public void go()
	{
	  if (codeFile == null)  {
		  System.out.println("TSC Assembler Error:  No filename was specified.");
			return;
		}

		new Assembler(codeFile, showSymbolTable, showSegmentInfo, debugMode).run();
		System.gc();   // Garbage collect for the hell of it
	}  // End of go




  /* Used by main when parsing the command line. */
  private static void usageError()  {
	  System.out.println("Usage Error: java -classpath <assemblerpath> " +
		  "TSCAssembler [-st|-seg] <filename>");
	}  // End of usageError




  /*  If main receives no arguements, it starts the GUI.  Otherwise 
	    it starts the assembler on the given file.  */
  public static void main(String args[])
  {
  	TSCAssembler tscAssembler = new TSCAssembler();
		
		
    // If there are no arguements then start the GUI
		if (args.length == 0)  {
		  System.out.println("Starting the TSC Assembler GUI");
      TSCAssemblerGUI gui = new TSCAssemblerGUI(tscAssembler);

      // Position the gui away from the edge of the screen
			Dimension screenSize = gui.getToolkit().getScreenSize();
			Dimension guiSize = gui.getSize();
			gui.setLocation((screenSize.width - guiSize.width) / 4,
			                (screenSize.height - guiSize.height) / 4);

			gui.show();
		}


    // The interface is being run in command line mode
		else  {

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
            
						// The show symbol table option.
            if (args[x].substring(1).compareToIgnoreCase("st") == 0)  {
						  tscAssembler.showSymbolTable = true;
							continue;  // Go to the next iteration of the loop
					  }
						
						// The show segment info option.
            if (args[x].substring(1).compareToIgnoreCase("seg") == 0)  {
						  tscAssembler.showSegmentInfo = true;
							continue;  // Go to the next iteration of the loop
					  }
						
						// The debug assembler option
            if (args[x].substring(1).compareToIgnoreCase("debug") == 0)  {
						  tscAssembler.debugMode = true;
							continue;  // Go to the next iteration of the loop
						}

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
					if (!tscAssembler.setCodeFile(new File(args[x])))  {
					  System.out.print("TSC Assembler Error:  " + args[x]);
						System.out.println(" could not be opened.");
						System.out.print("  Make sure the file exists and has a .tsc");
						System.out.println(" extension");
				  }
					else
					  tscAssembler.go();
				}
			}

      if (!foundFile)  {
				usageError();
				System.exit(1);
		  }
		}
		
  } // End of main

}  // End of TSCAssembler class

