/*
© 2000, Performance Evaluation Laboratory, Brigham Young University. Reproduction of all or part of this work is permitted for non-profit educational
or research use provided this copyright notice remains intact. All other rights reserved. 
*/

import java.io.*;
import java.awt.Dimension;

/* This class prints a hex dump of a TSC binary file.  It can be 
   executed directly from a command line or called from another 
	 program.  */

class TSCBinFileViewer
{


  /* Prints a hex dump of 'binaryFile'.  'binaryFile' should not 
	   be null.  */
	public static void viewFile(File binaryFile)
	{
		int lineNumber = 0;
    StringBuffer buffer = new StringBuffer();
    RandomAccessFile binFileStream;

    try {
  		binFileStream = new RandomAccessFile(binaryFile, "r");
		} catch (IOException e)  {
			  System.out.println("I/O Exception occurred while opening " 
				                                         + e.getMessage());
				return;
		}
		
		while (true)  {
      buffer.setLength(0);
		  buffer.append(Tools.intToHexString(lineNumber) + ": ");
			try  {
				for (int j = 0; j < 8; j++)
          buffer.append(Tools.shortToHexString(binFileStream.readShort()) + " ");
			  System.out.println(buffer.toString());
      } catch (IOException e)  {
			    try  {
    			  System.out.println(buffer.toString());
			      binFileStream.close();
						return;
					} catch (IOException ioe)  {
					    System.out.println("Could not close " + binaryFile.getName());
					}
			}
			lineNumber += 8;
		}
	}  // End of viewFile




  /* Used by main when parsing the command line */
  private static void usageError()  {
	  System.out.println("Usage Error: java -classpath <path> TSCBinFileViewer <filename>");
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
					  System.out.print("TSCBinFileViewer Error:  " + args[x]);
						System.out.println(" could not be opened.");
						System.out.print("  Make sure the file exists and has a .bin");
						System.out.println(" extension");
				  }
					else
					  viewFile(new File(args[x]));
				}
			}

      if (!foundFile)  {
				usageError();
				System.exit(1);
		  }
		}
		
  } // End of main

}  // End of TSCBinFileViewer class

