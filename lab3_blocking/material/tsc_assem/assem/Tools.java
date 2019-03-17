/*
© 2000, Performance Evaluation Laboratory, Brigham Young University. Reproduction of all or part of this work is permitted for non-profit educational
or research use provided this copyright notice remains intact. All other rights reserved. 
*/

import java.io.*;
import java.util.Vector;


/*  Misc. methods for the TSC Assembler.
    This class is not designed to be instantiated so all 
		methods must be static. */

final class Tools
{
  final static String NEWLINE = System.getProperty("line.separator");
	final static String mnemonicStrings[] = Assembler.mnemonicStrings;



  /* Returns true if the string contains a valid integer value.
     Both decimal and hex strings are accepted. */
	public static boolean isNumber(String number)
	{
  	// If 'number' is in hex, then the '0x' must be lower case
    String newNumber = number.toLowerCase();
		try  {
  		Integer.decode(newNumber).intValue();
    } catch (NumberFormatException e)  {
        return false;     // String was not a number
	  }
		return true;
  }  // End of isNumber




  /* Takes a hex or decimal number in string form 
	   and returns a short.  Hex numbers must have 
		 a '0x' prefix.  The string is assumed to contain
		 a valid number. */
  public static short stringToShort(String number)
	{
    int x = 0;

    // If the number is in hex, the "0x" must be lower case
    number = number.trim().toLowerCase();
		if (number.equals("0x"))   // 0x is interpreted as a 0
      return (short) x;

    try {  
      x = Integer.decode(number).shortValue();
    } catch (NumberFormatException e)  {
        // This error should not be reached
        System.out.println("NumberFormatException in Tools.stringToShort: "
				                    + e.getMessage());
    }
		
		return (short) x;
	}   // End of stringToShort




  /* Takes a hex or decimal number in string form 
	   and returns an int.  Hex numbers must have 
		 a '0x' prefix.  The string is assumed to contain
		 a valid number i.e. isNumber should be called 
		 first. */
  public static int stringToInt(String number)
	{
    int x = 0;

    // If the number is in hex, the "0x" must be lower case
    number = number.trim().toLowerCase();
		if (number.equals("0x"))   // 0x is interpreted as a 0
      return x;

    try {  
      x = Integer.decode(number).intValue();
    } catch (NumberFormatException e)  {
        // This error should not be reached
        System.out.println("NumberFormatException in Tools.stringToInt: "
				                    + e.getMessage());
    }
		
		return x;
	}   // End of stringToInt




  /* Takes an int and returns its hex representation in a string.
	   The string is padded with zeroes to 8 places. */
  public static String intToHexString(int x)
	{
	  String hexNumber = Integer.toHexString(x);
    int length = hexNumber.length();
		String zeroStr = new String("");
    StringBuffer buffer = new StringBuffer();
		
		if (length < 8)  {
		  for (int y=0;  y < 8-length; y++)
			  buffer.append("0");
			zeroStr = buffer.toString();
			buffer.setLength(0);
		}

    buffer.append(zeroStr).append(hexNumber);
		return buffer.toString();

  }  // End of intToHexString




  /* Takes a short and returns its hex representation in a string.
	   The string is padded with zeroes to 4 places. */
  public static String shortToHexString(short x)
	{
		String zeroStr = new String("");
    StringBuffer buffer = new StringBuffer();

    String hexNumber = Integer.toHexString((int) x);
    int length = hexNumber.length();
		if (length < 4)  {
		  for (int y=0;  y < 4-length; y++)
			  buffer.append("0");
			zeroStr = buffer.toString();
			buffer.setLength(0);
		}

		if (length > 4)
		  // this occurs when x > 32767 causing it to be 
			// sign-extended to 8 hex digits.
			hexNumber = hexNumber.substring(4);

    buffer.append(zeroStr).append(hexNumber);
		return buffer.toString();

  }  // End of shortToHexString




  /* Takes a byte and returns its hex representation in a string.
	   The string is padded with zeroes to 2 places. */
  public static String byteToHexString(byte x)
	{
		String zeroStr = new String("");
    StringBuffer buffer = new StringBuffer();

    String hexNumber = Integer.toHexString((int) x);
    int length = hexNumber.length();
		if (length < 2)  {
		  for (int y=0;  y < 2-length; y++)
			  buffer.append("0");
			zeroStr = buffer.toString();
			buffer.setLength(0);
		}

		if (length > 2)
		  // this occurs when x > 127 causing it to be 
			// sign-extended to 8 hex digits
			hexNumber = hexNumber.substring(6);

    buffer.append(zeroStr).append(hexNumber);
		return buffer.toString();

  }  // End of byteToHexString




	/*  Used during debugging only i.e. DEBUG = true. 
	    The attributes for each instruction are printed in a 
			file named <filename>.debug. */
	public static void debugPrint(CodeLine cl, String filename, PrintStream out)
	{
		RandomAccessFile file;
		
		try {
			file = new RandomAccessFile(filename, "rw");
		} catch (IOException e)  {
		    out.println("Error opening " + filename + ": " + e.getMessage());
				return;
		}
		
		try {
			file.seek(file.length());
		} catch (IOException e)  {
		    out.println("Error seeking in " + filename + ": " + e.getMessage());
				return;
		}
		
		try {
			file.writeBytes("Original code: " + cl.originalCode + NEWLINE);
			file.writeBytes("Line Number: " + cl.lineNumber + NEWLINE);
			file.writeBytes("Address: " + cl.address + NEWLINE);
			file.writeBytes("mnemonic: " + mnemonicStrings[cl.mnemonic] + NEWLINE);
			file.writeBytes("Label: " + cl.label + NEWLINE);
			file.writeBytes("Operand String: " + cl.operandString + NEWLINE);
			file.writeBytes("Operand Value: " + cl.operandValue + NEWLINE);
			file.writeBytes("Directive: " + cl.directive + NEWLINE);
			file.writeBytes("Pseudo-Instruction: " + cl.pseudoInstr + NEWLINE);
			file.writeBytes("Binary Instruction: 0x" + 
			        Integer.toHexString(cl.binaryInst & 0x0000ffff) + NEWLINE);
			if ((cl.operandList != null) && (cl.operandList.size() > 0))
				for (int i = 0; i < cl.operandList.size(); i++)
					file.writeBytes("List item " + (i + 1) + ": " 
					    + ((String) cl.operandList.elementAt(i)) + NEWLINE);
			if (cl.error)
				file.writeBytes("Error Message: " + cl.errorMessage + NEWLINE);
			file.writeBytes(NEWLINE);
		} catch (IOException e)  {
		    out.println("Error writing to " + filename + ": " + e.getMessage());
				return;
		}
		
		try {
			file.close();
		} catch (IOException e)  {
		    out.println("Error closing " + filename + ": " + e.getMessage());
				return;
		}
		
	}  // End of debugPrint

}  // End of Tools class

