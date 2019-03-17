/*
© 2000, Performance Evaluation Laboratory, Brigham Young University. Reproduction of all or part of this work is permitted for non-profit educational
or research use provided this copyright notice remains intact. All other rights reserved. 
*/

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;


/*  The Memory class holds the contents of memory.  Even though the 
    memory contents are shorts, all public methods that require 
		addresses or memory values as parameters must read and/or return 
		them as integers.  This avoids the automatic casting of shorts 
		to integers that Java performs when shorts are used in arithmetic 
		operations.  
		
		Memory is a subclass of AbstractListModel so that it can be 
		dropped into a JList.  Updating memory automatically updates any 
		displays.  */

public class Memory extends AbstractListModel
{
	public final static int HEX = Constants.HEX;
	public final static int DECIMAL = Constants.DECIMAL;
	public final static int SIGNED_DECIMAL = Constants.SIGNED_DECIMAL;
  public final static int NO_HIGHLIGHT = Constants.NO_HIGHLIGHT;
  public final static String COLON = ": ";
  public final static int ROM_MAGIC_NUMBER = Constants.ROM_MAGIC_NUMBER;
  public final static int MEMSIZE = 65536;
  private static String errorString[] = new String[3];
	
	// Temporary static buffer used to append strings together.  This 
	// avoids having to create a new string buffer every time one is needed.
	private static StringBuffer buffer = new StringBuffer();
	
	// Used to highlight the memory location of the 
	private NotifyHlightMem notifyHighlightMem = new NotifyHlightMem();
	
  private Display display;
	
  // mem holds the actual contents of memory.
	private short[] mem = new short[MEMSIZE];
	public int memAddressFormat = HEX;  // The DebugConsole uses this variable
	private int memContentsFormat = HEX;




	public Memory()
	{
    System.out.println("Initializing memory...");
	}  // End Constructor



	
	/* Called by Display to inform Memory where the display is. */
	public void setDisplay(Display display)
	{
	  this.display = display;
	}  // End of setDisplay




  // Used by JList
	public int getSize()
	{
		return mem.length;
	}




  // Used by JList
  public Object getElementAt(int index)
	{
    int value = mem[index] & 0x0000ffff; // used for indexing
    buffer.setLength(0);

    // Update strings
    switch (memContentsFormat)  {
			
      case HEX:  
        if (memAddressFormat == HEX)
          return buffer.append(Formatter.intToShortHexString(index)).append(COLON)
					                .append(Formatter.intToShortHexString(value)).toString();
				else if (memAddressFormat == DECIMAL)
          return buffer.append(index).append(COLON)
					    .append(Formatter.intToShortHexString(value)).toString();
 				break;

 			case DECIMAL:
        if (memAddressFormat == HEX)
          return buffer.append(Formatter.intToShortHexString(index))
					                              .append(COLON).append(value).toString();
 				else if (memAddressFormat == DECIMAL)
          return buffer.append(index).append(COLON).append(value).toString();
 				break;

 			case SIGNED_DECIMAL:
        if (memAddressFormat == HEX)
          return buffer.append(Formatter.intToShortHexString(index))
					                      .append(COLON).append((short) value).toString();
 				else if (memAddressFormat == DECIMAL)
          return buffer.append(index).append(COLON).append((short) value).toString();
 				break;

      default:
  	    System.out.println("Error in Memory.getElementAt(): Invalid format");
				
  	}  // End of case statement

		return "Error in Memory.getElementAt()";

  }  // End of getElementAt




  /* Returns the value at mem[address].  The high 16 bits of the 
	   returned integer will always be zeroes to avoid sign 
		 extending negative shorts.
		 'accessType' is used by the GUI for highlighting purposes. 
		 All reads from memory use this method so that the highlighted 
		 display is updated.  */
  public int readMem(int address, int accessType)
	{
	  address = address & 0x0000ffff;
    
		if (display.tscConsole.highlightMemStatus != NO_HIGHLIGHT)	{
		  notifyHighlightMem.setAddress(address, accessType);
  		SwingUtilities.invokeLater(notifyHighlightMem);
		}
    
		return mem[address] & 0x0000ffff;
	}  // End of readMem

	
 	
	
  /* Writes 'value' into mem[address].  The high 16 bits of 
	   'value' will always be dropped.
		 'accessType' is used by the GUI for highlighting purposes. 
		 All writes to memory use this method so that the highlighted 
		 display is updated.  */
  public void writeMem(int address, int value, int accessType)
	{
	  address = address & 0x0000ffff;
	
    // Write to memory
		mem[address] = (short) value;

    // Update display
	  fireContentsChanged(this, address, address);
    if (display.tscConsole.highlightMemStatus != NO_HIGHLIGHT)	{
		  notifyHighlightMem.setAddress(address, accessType);
  		SwingUtilities.invokeLater(notifyHighlightMem);
		}
		  
	}  // End of writeMem
	


	
	/*  Nested class that is inserted into the event thread by 
	    writeMem() and readMem().  It notifies the display that 
			a memory access has been made so the appropriate memory 
			highlighting can be done.*/
  private class NotifyHlightMem implements Runnable
	{
    private int accessType;
    private int addr;

		public void setAddress(int addr, int accessType)
		{
		  this.accessType = accessType;
			this.addr = addr;
		}
		
		public void run()
		{
 			display.tscConsole.highlightMem(addr, accessType);
		}
  }  // End of NotifyMemoryAccess
	


	
  /* Updates the address format.*/
	public void updateMemAddressFormat(int newFormat)
	{
	  memAddressFormat = newFormat;
	  fireContentsChanged(this, 0, MEMSIZE - 1);
	}  // End of updateMemAddressFormat
	
	

	
  /* Updates the format of the memory contents.*/
	public void updateMemContentsFormat(int newFormat)
	{
	  memContentsFormat = newFormat;
	  fireContentsChanged(this, 0, MEMSIZE - 1);
	}  // End of updateMemContentsFormat




  /* Zeroes out memory. */
	public void clearMem()
	{
	  for (int i = 0; i < MEMSIZE; i++)
      mem[i] = 0;
	  fireContentsChanged(this, 0, MEMSIZE - 1);
	}  // End of clearMem




  /* Opens a ROM file and loads it into memory. The first 4 bytes of 
	   the file contain the magic number.  They are followed by 4 bytes 
		 containing the starting index in memory and 4 bytes containing 
		 the ending index.  The data then follows in 2 byte increments.*/
	public void loadRom(File file)
	{
    DataInputStream in;
		
    // Create data stream to read shorts
		try {
  	   in = new DataInputStream(new FileInputStream(file));
		} catch (FileNotFoundException e)  {
				errorString[0] = file.getAbsolutePath();
				errorString[1] = "is not there";
				errorString[2] = e.getMessage();
        new OkDialog(display, true).show("Load ROM: File Missing Error",
				                                                    errorString);
 		    return;
		}

		try {   
		  // Check magic number
  		if (in.readInt() != ROM_MAGIC_NUMBER)  {
				errorString[0] = file.getAbsolutePath();
				errorString[1] = "is not a ROM file";
				errorString[2] = null;
        new OkDialog(display, true).show("Load ROM: Magic Number Error",
				                                                    errorString);
		  }
			else {
	      // Get the starting and ending indices
				int start = in.readInt();
				int end = in.readInt();
	
	      // Load file into the correct portion of memory and zero the rest (why?)
	      int i = start;
				for (i = 0; i < start; i++)
				  mem[i] = 0;
	  		for (; i <= end; i++)
				  mem[i] = in.readShort();
				for (; i < MEMSIZE; i++)
				  mem[i] = 0;
			}
		} catch (IOException e)  {   // Also catches EOFException
				errorString[0] = "Error in reading";
				errorString[1] = file.getAbsolutePath();
				errorString[2] = e.getMessage();
        new OkDialog(display, true).show("I/O Error", errorString);
		
		} finally {
        // The mem contents changed so update the display
        fireContentsChanged(this, 0, MEMSIZE - 1);

			  // Close the file
				try {
				  in.close();
				} catch (IOException e) {
					errorString[0] = "Error in closing";
					errorString[1] = file.getAbsolutePath();
					errorString[2] = e.getMessage();
	        new OkDialog(display, true).show("I/O Error", errorString);
				}
		}

  }  // End of loadRom
	



  /* Opens a ROM file and writes memory into it. To avoid creating 
	   128Kb files, only the portion of memory starting at the first 
		 non-zero value and ending at the last non-zero value is written
		 to the file.*/
	public void makeRom(File file)
	{
    DataOutputStream out;
		
    // Create data stream that writes shorts
		try {
  	   out = new DataOutputStream(new FileOutputStream(file));
		} catch (IOException e)  {
				errorString[0] = "Error in writing to";
				errorString[1] = file.getAbsolutePath();
				errorString[2] = e.getMessage();
        new OkDialog(display, true).show("Make ROM: Write File Error",
				                                                  errorString);
			  return;
		}

    // Find first and last non-zero values in memory
		int start = 0;
		int end = MEMSIZE -1;
		while ((mem[start] == 0) && (start < end))  start++;
		while ((mem[end]   == 0) && (end > start))  end--;

    // Write memory to file
		try {
  		out.writeInt(ROM_MAGIC_NUMBER);
			out.writeInt(start);
			out.writeInt(end);

  		for (int i = start; i <= end; i++)
			  out.writeShort(mem[i]);
      out.close();

		} catch (IOException e)  {   // Also catches EOFException
				errorString[0] = "Error in writing to";
				errorString[1] = file.getAbsolutePath();
				errorString[2] = e.getMessage();
        new OkDialog(display, true).show("Make ROM: I/O Error", errorString);
		} finally {
			  // Close the file
				try {
				  out.close();
				} catch (IOException e) {
					errorString[0] = "Error in closing";
					errorString[1] = file.getAbsolutePath();
					errorString[2] = e.getMessage();
	        new OkDialog(display, true).show("I/O Error", errorString);
				}
		}
	}  // End of makeRom

} // End of Memory class

