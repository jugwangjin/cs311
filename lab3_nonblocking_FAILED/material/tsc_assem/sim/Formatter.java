/*
© 2000, Performance Evaluation Laboratory, Brigham Young University. Reproduction of all or part of this work is permitted for non-profit educational
or research use provided this copyright notice remains intact. All other rights reserved. 
*/


/*Number formatter and error checking class. */


final class Formatter
{
  private static StringBuffer buffer = new StringBuffer();
    // buffer is used to prevent the compiler from creating a 
    // new StringBuffer object each time through the loops below.




  /* Takes a hex or decimal number in string form and returns an 
	   integer with the high 16 bits zeroed out.  Hex numbers must 
		 have a '0x' prefix.  The string is assumed to contain a 
		 valid number. */
  public static int stringToShortInt(String number)
	{
		return stringToInt(number) & 0x0000ffff;
	}   // End of stringToShortInt




  /* Takes a hex or decimal number in string form 
	   and returns an int.  Hex numbers must have 
		 a '0x' prefix.  The string is assumed to contain
		 a valid number. */
  public static int stringToInt(String number)
	{
    int x = 0;

    number = number.trim().toLowerCase();  // The 'x' in '0x' must be lower case
		if (number.equals("0x"))   // 0x is interpreted as a 0
      return x;

    try {  
      x = Integer.decode(number).intValue();
    } catch (NumberFormatException e)  {
        System.out.println("NumberFormatException in Formatter.stringToInt: "
				                    + e.getMessage());
      }
		
		return x;
	}   // End of stringToInt




  /* Takes an integer and returns the hex representation of the low 16 
	   bits in a string.  The string has a '0x' prefix and is padded with 
		 zeroes to 4 places.  'x' cannot be null.  */
  public static String intToShortHexString(int x)
	{
	  String hexNumber = Integer.toHexString(x);
		buffer.setLength(0);
		
	  switch (hexNumber.length())  {
		  case 1:
			  return buffer.append("0x000").append(hexNumber).toString();
			case 2:
			  return buffer.append("0x00").append(hexNumber).toString();
			case 3:
			  return buffer.append("0x0").append(hexNumber).toString();
			case 4:
			  return buffer.append("0x").append(hexNumber).toString();
			default:
			  // length > 4
				return buffer.append("0x").append(hexNumber.substring(4)).toString();
		}

  }  // End of intToShortHexString


	
	
  /* Takes an integer and returns the binary representation of the low 
	   16 bits in a string. The string is padded with zeroes to 16 places. */
	public static String intToShortBinaryString(int x)
	{
	  String binaryNumber = Integer.toBinaryString(x);
    int length = binaryNumber.length();
		String zeroStr = new String("");
		buffer.setLength(0);
		
		if (length < 16)  {
		  for (int y=0;  y < 16-length; y++)
			  buffer.append(0);
			zeroStr = buffer.toString();
			buffer.setLength(0);
    }
		
		if (length > 16)
			binaryNumber = binaryNumber.substring(16);
		
    buffer.append(zeroStr).append(binaryNumber);
		return buffer.toString();
  }  // End of intToShortBinaryString
	  

	
	
  /* Returns true if a string represents a valid hex or 
	   decimal short.	*/
  public static boolean isStringValidHexOrDec(String number)
	{
		if (number.equals("0x"))  // 0x is just 0
		  return true;
			
		// Check for leading multiple zeroes i.e. 00
		if (number.startsWith("00"))
		  return false;
			
    // Check for 1 leading zero but no following 'x' i.e. 012
		if (number.startsWith("0") && !number.startsWith("0x")
		                           && (number.length() > 1))
      return false;

		// Check for everything else
		try  {
  		Integer.decode(number).intValue();
    } catch (NumberFormatException e)  {
        return false; // String was not a number i.e. 01, 02, etc.
	  }
		
		return true;
  }  // End of isStringValidHexorDec




	/* Returns true if a char can be appended onto the string 
	   to form a valid hex or decimal short.  The string must
		 already be in a valid number format.  A hex number must 
		 have a leading '0x' or it is considered to be a decimal 
		 number.  Only 4 hex digits or 5 decimal digits are allowed. 
		 If the decimal number is greater than 65535 than it is 
		 not a valid short.*/
	public static boolean isCharValid(String number, char c)
	{
		c = Character.toLowerCase(c);
		
		if (((c < '0') || (c > '9')) && ((c < 'a') || (c > 'f')) && (c != 'x'))
			return false;  // Invalid character
		
		int length = number.length();
		
		if (length == 0)  {
		  if ((c < '0') || (c > '9'))
			  return false;   // Invalid starting character
			else 
			  return true;    // Character is valid
		}
		
  	if (number.equals("0"))  {
		  if (c != 'x')
			  return false;   // Invalid second character for hex number
			else 
			  return true;    // Character is valid
		}
		
		if (number.startsWith("0x"))  {
		  if ((length > 5) || (c == 'x'))
		    return false;      // Invalid hex character
   		else
		    return true;
		}
		
		// number must be a decimal if this code is reached
		if ((length > 4) || (c < '0') || (c > '9'))
		  return false;
		else 
		  return true;

  }	 // End of isCharValid

}  // End of Formatter class

