/*
© 2000, Performance Evaluation Laboratory, Brigham Young University. Reproduction of all or part of this work is permitted for non-profit educational
or research use provided this copyright notice remains intact. All other rights reserved. 
*/

import java.io.*;
import java.awt.Dimension;
import java.util.Vector;

/* This class converts a TSC binary file to an Intel MDS data 
   file.  It can be executed directly from a command line or
	 called from another program.  */

class TSCBinFileToHexFile
{
  final static String NEWLINE = System.getProperty("line.separator");


	/* Returns true if the binary file is successfully converted 
	   to an Intel MDS hex data file.  Returns false otherwise.  
		 The hex file is named <binFile>.hex and is placed in the 
		 same directory as the binary file. 'binFile' should not 
		 be null.  */
	public static boolean convert(File binFile)
	{
    // The number of data bytes per .hex line.  This number must fit 
		// into 8 bits.
		final int DATA_BYTES_PER_LINE = 2 & 0x000000ff;
		
		RandomAccessFile binFileStream;
    RandomAccessFile hexFileStream;
		File hexFile = new File(FileTools.replaceExtension(
		                 binFile.getAbsolutePath(), "hex"));
		StringBuffer buffer = new StringBuffer(":"); // All lines start with a colon

    if (hexFile.exists())  // Delete an existing .hex file
      hexFile.delete();

		try  {
  		binFileStream = new RandomAccessFile(binFile, "r");
  		hexFileStream = new RandomAccessFile(hexFile, "rw");
		} catch (IOException e)  {
			  System.out.println("I/O Exception occurred while accessing " 
				                                           + e.getMessage());
				return false;
		}

    try  {
      int numSegments = 0;
			int segAddress = 0;
			int numShorts = 0;
			int aShort = 0;

      // Get the number of segments
      numSegments = binFileStream.readShort() & 0x0000ffff;
			
      // Read in the segment info
			for (int seg = 0; seg < numSegments; seg++)  {
        segAddress = binFileStream.readShort() & 0x0000ffff;
        numShorts = binFileStream.readShort() & 0x0000ffff;

        // Convert segAddress from a word address to a byte address.  
				segAddress *= 2;
				
        // Output one .hex line for every short in the segment
				for (int i = 0; i < numShorts; i++)  {
				  
          aShort = binFileStream.readShort() & 0x0000ffff;
					
					// Create the string representing the .hex line.
          // All lines start with a colon.
          buffer.setLength(1);
					// The first hex pair is the number of data bytes on the line.  
					buffer.append(Tools.byteToHexString((byte) DATA_BYTES_PER_LINE));
					// The next 2 hex pairs indicate the starting address of the 
					// data bytes.
					buffer.append(Tools.shortToHexString((short) segAddress));
					// The next hex pair is always 0x00 except for the last line 
					// in the file.
					buffer.append("00");
					// The data in Little Endian format.
					buffer.append(Tools.byteToHexString((byte) aShort));
					buffer.append(Tools.byteToHexString((byte) (aShort >> 8)));
					/* Compute the 8 bit checksum and write it to the file.
					   The 8-bit sum of all the hex pairs on the line 
						 (including the checksum) must be zero.  */
					buffer.append(Tools.byteToHexString(
					  (byte) (256 - (byte) DATA_BYTES_PER_LINE - (byte) segAddress - 
						(byte) (segAddress >> 8) - (byte) aShort - (byte) (aShort >> 8))));
					
					/* Write the line to the .hex file.  The writeBytes method 
					   writes the last 8 bits of each character in a string (a 
						 normal Java character has 16 bits.) */
				  buffer.append(NEWLINE);
					hexFileStream.writeBytes(buffer.toString().toUpperCase());
					
					// Increcement the byte address
					segAddress += 2;
				}
			}
			
			// Write the terminating line to the .hex file
			hexFileStream.writeBytes(":00000001FF");
			
		} catch (IOException e)  {
			  System.out.println("I/O Exception occurred during conversion: " 
				                                           + e.getMessage());
				return false;
		} finally  {
		    try  {
				  binFileStream.close();
				  hexFileStream.close();
				} catch (IOException ioe)  {
  			    System.out.println("Error while closing " + ioe.getMessage());
						return false;
				}
		}
		
		return true;

	}  // End of convert




  /* Used by main when parsing the command line */
  private static void usageError()  {
	  System.out.println("Usage Error: java -classpath <path> TSCBinFileToHexFile <filename>");
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
						System.out.println(" All options must be placed before the filename");
						System.exit(1);
					}
					if (!FileTools.getExtension(args[x]).equalsIgnoreCase("bin"))  {
					  System.out.print("TSCBinFileToHexFile Error:  " + args[x]);
						System.out.println(" could not be opened.");
						System.out.print("  Make sure the file exists and has a .bin");
						System.out.println(" extension");
				  }
					else  {
			      System.out.println("Converting " + args[x] + " to " +
		            FileTools.replaceExtension(args[x], "hex") + " ...");
					  if (convert(new File(args[x])))
						  System.out.println("Conversion complete.");
						else
						  System.out.println("Conversion failed.");
					}
				}
			}

      if (!foundFile)  {
				usageError();
				System.exit(1);
		  }
		}
  } // End of main

}  // End of TSCBinFileToHexFile class

