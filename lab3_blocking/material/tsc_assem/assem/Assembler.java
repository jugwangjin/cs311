/*
© 2000, Performance Evaluation Laboratory, Brigham Young University. Reproduction of all or part of this work is permitted for non-profit educational
or research use provided this copyright notice remains intact. All other rights reserved. 
*/

import java.io.*;
import java.util.Vector;

/* The TSC Assembler for CS 143 at BYU.
   Written by Jeff Penfold during June 1999. 
   
   I have never taken a compiler class and this is my 
   second stab at an assembler.  So I apologize if this 
   code is hard to follow.  
  
   If you want to add an instruction (the TSC instruction 
   set can hold a total of 79 instructions) or a 
   directive, good luck! */

class Assembler
{
  /* The value for each instruction below is it's index in 
	   "mnemonicStrings", "opcodes" and "numOfOperands".  It 
	   is not the opcode!  */
  final static int ADD = 0;     // R-format instructions
  final static int SUB = 1;
  final static int AND = 2;
  final static int ORR = 3;
  final static int NOT = 4;
  final static int TCP = 5;
  final static int SHL = 6;
  final static int SHR = 7;
  final static int RWD = 8;
  final static int WWD = 9;
  final static int JPR = 10;
  final static int JRL = 11;
  final static int HLT = 12;
  final static int ENI = 13;
  final static int DSI = 14;
  final static int ADI = 15;    // I-format instructions
  final static int ORI = 16;
  final static int LHI = 17;
  final static int LWD = 18;
  final static int SWD = 19;
  final static int BNE = 20;
  final static int BEQ = 21;
  final static int BGZ = 22;
  final static int BLZ = 23;
  final static int JMP = 24;    // J-format instructions
  final static int JAL = 25;
  /* The value of any directive or pseudo-instruction must be greater 
     than any of the instruction values.  Also, the value of ORG must 
     be one greater than the largest instruction value. */
  final static int ORG = 26;  // Directive
  final static int END = 27;  // Directive
  final static int EQU = 28;  // Directive
  final static int BSS = 29;  // Directive
  final static int BSC = 30;  // Directive
  final static int LAD = 31;  // Pseudo-instruction
  // The value of "INVALID" must be the last index in "mnemonicStrings"
  final static int INVALID = 32;
               
  // The mnemonic for all instructions, pseudo-instructions, and
	// directives.
  final static String mnemonicStrings[] = new String[]
    {"ADD", "SUB", "AND", "ORR", "NOT", "TCP", 
		 "SHL", "SHR", "RWD", "WWD", "JPR", "JRL", 
		 "HLT", "ENI", "DSI", "ADI", "ORI", "LHI", 
		 "LWD", "SWD", "BNE", "BEQ", "BGZ", "BLZ", 
		 "JMP", "JAL", 
		 ".ORG", ".END", ".EQU", ".BSS", ".BSC", 
		 "LAD", "INVALID"};
      
  // The opcodes and function codes for all "real" intstructions.
	final static int opcodes[] = new int[]
    {0x0000f000, 0x0000f001, 0x0000f002, 0x0000f003, 0x0000f004, 0x0000f005, 
     0x0000f006, 0x0000f007, 0x0000f01b, 0x0000f01c, 0x0000f019, 0x0000f01a, 
		 0x0000f01d, 0x0000f01e, 0x0000f01f, 0x00004000, 0x00005000, 0x00006000, 
		 0x00007000, 0x00008000, 0x00000000, 0x00001000, 0x00002000, 0x00003000, 
		 0x00009000, 0x0000a000,};
	
	/* The number of operands each instruction or directive must have.  
	   A -1 means that the number of operands is dependent on certain 
		 conditions or is irrelevant.  */
	final static int numOfOperands[] = new int[]
	  { 3,  3,  3,  3,  2,  2, 
		  2,  2,  1,  1,  1,  1, 
		 -1, -1, -1,  3,  3,  2,
		  3,  3,  3,  3,  2,  2,
			1,  1,  
			1, -1,  1,  1, -1, 
			2, -1};

  final static String NEWLINE = System.getProperty("line.separator");
  final static int MAXMEMSIZE = 65535;
  final static boolean SYM_ADDR = false;
  final static boolean LABEL = true;
	final static int IMMEDMASK = 0x000000ff;
	final static int TARGETMASK = 0x00000fff;
	final static int HIGHHALFMASK = 0x0000ff00;
	final static int LOWHALFMASK = 0x000000ff;

  private File codeFile;
  private File debugFile;
  private PrintStream out;
	private boolean showSymbolTable;  // Print symbol table if true
	private boolean showSegmentInfo;  // Print segment info if true
	private boolean debugMode;  // Dump tscCode to a .debug file if true
  private Vector tscCode;
  private SymbolTable symbolTable;
  private SegmentManager segMan;
  private int errorCount = 0;     // Total number of errors




  public Assembler(File codeFile, boolean showSymbolTable, 
	              boolean showSegmentInfo, boolean debugMode)
  {
    this.codeFile = codeFile;
		this.showSymbolTable = showSymbolTable;
		this.showSegmentInfo = showSegmentInfo;
		this.debugMode = debugMode;
    this.out = System.out;
    this.tscCode = new Vector(100, 50);
    this.symbolTable = new SymbolTable();
    this.segMan = new SegmentManager();
  
    if (debugMode)  {        // Erase old ".debug" file
      this.debugFile = new File
			  (FileTools.replaceExtension(codeFile.getAbsolutePath(), "debug"));
      if (this.debugFile.exists())
        this.debugFile.delete();
    }
  }  // End of constructor




  /* The code file is loaded into a buffer, run through the 
     first and second passes of the assembler, and written to
     a .bin file.  If any errors are found during the first pass 
     the second pass is not performed.  Likewise, if any errors 
     are found during the second pass, the .bin file is not 
     created.  */

  public void run()
  {
    // Open the code file
    out.println(NEWLINE + "Assembling " + codeFile.getName() + "...");
    RandomAccessFile fileStream;
    try {
      fileStream = new RandomAccessFile(codeFile, "r");
    } catch (IOException e)  {
        out.println("I/O Exception occurred while opening " + e.getMessage());
        return;
    }

        
    // Load the file contents into a buffer
    int lineNumber = 1;
    try {
	    String tempLine = null;

      tempLine = fileStream.readLine();
      while (tempLine != null)  {
        tempLine = tempLine.trim();
        if (tempLine.length() != 0)  {             // Line is not empty
          // Ignore comment lines
          if ((tempLine.charAt(0) != '*') && (tempLine.charAt(0) != ';'))  {
            CodeLine cl = new CodeLine(tempLine);
            cl.lineNumber = lineNumber;
            tscCode.addElement(cl);
          }
        }
        tempLine = fileStream.readLine(); // Read next line
        lineNumber++;
      }
    } catch (IOException e)  {
        out.println("I/O Exception occurred while reading " + 
				  codeFile.getAbsolutePath() + ":  " + e.getMessage());
        return;
    } finally {
        try  {  fileStream.close();  }   // Close the code file
        catch (IOException ie)  {
          out.println("Could not close " + codeFile.getAbsolutePath() + ":  " 
                                                   + ie.getMessage());
        }
    }


    // First pass
    out.println("Start first pass...");
    firstPass();
    if (errorCount > 0)  {
      out.println(NEWLINE + errorCount + " error(s) found during the first pass.");
      out.println("  Exiting TSC Assembler.");
      return;
    }


    // Second pass
    out.println("Start second pass...");
    secondPass();
    if (errorCount > 0)  {
      out.println(NEWLINE + errorCount + " error(s) found during the second pass.");
      out.println("  Exiting TSC Assembler.");
      return;
    }


    // Make the .debug file
    if (debugMode)  {
      out.println("Creating " + debugFile.getName() + " ...");
      for (int i = 0; i < tscCode.size(); i++)
        Tools.debugPrint((CodeLine) tscCode.get(i), 
					debugFile.getAbsolutePath(), out);
    }


    // Open the codeFile.bin file
    File binaryFile = new File(FileTools.replaceExtension(
                       codeFile.getAbsolutePath(), "bin"));
    if (binaryFile.exists())  // Delete an existing .bin file
      binaryFile.delete();
    out.println("Creating " + binaryFile.getName() + " ...");
    RandomAccessFile binFileStream;
    try {
      binFileStream = new RandomAccessFile(binaryFile, "rw");
    } catch (IOException e)  {
        out.println("I/O Exception occurred while creating " + 
                 binaryFile.getAbsolutePath() + ":  " + e.getMessage());
        return;
    }


    /* Write the binary instructions in tscCode to a codeFile.bin file.

       The 1st short in the file is the number of memory segments.  The 
       remainder of the file contains the segments in sequential order. 
       The format for each segment is:
       1st short: the segment's starting address
       2nd short: the number of shorts in the segment
       Remainder: the shorts in the segment  
      
       The order of the code lines in tscCode corresponds to the order 
       of the segments in segmentList i.e. if the first segment starts at 
       location 35 and ends at location 40 then the code lines can simply be 
       taken from tscCode starting at the beginning until location 40 is 
       reached.  The next segment is then examined and the next line of code 
       is read from tscCode starting right after the last line of code from 
       the last segment. */
      
    Vector segmentList = segMan.getSegmentList();

    try {
      binFileStream.writeShort(segmentList.size());  // Write number of segments

      int clIndex = 0;       // The index of the current codeline in tscCode
      for (int line = 0; line < segmentList.size(); line++)  {
        Segment curSegment = (Segment) segmentList.get(line);
        int segmentSize = curSegment.end - curSegment.begin + 1;
        binFileStream.writeShort(curSegment.begin);   // Write starting address
        binFileStream.writeShort(segmentSize);        // Write size of the segment

        int locationsUsed = 0; // The # of locations processed in the current segment
        while (locationsUsed < segmentSize)  {
          CodeLine cl = (CodeLine) tscCode.get(clIndex);
          if (!cl.directive)  {  // Write the binary instruction
            binFileStream.writeShort(cl.binaryInst);
            clIndex++;
            locationsUsed++;
          }
          else  {
            // The code is a directive so multiple memory locations may be needed
            switch (cl.mnemonic)  {

              case BSC:  
                // Write the constants in cl.list to the bin file
                for (int k = 0; k < cl.operandList.size(); k++)  {
                  binFileStream.writeShort(
                    Tools.stringToShort((String) cl.operandList.get(k)));
                }
                locationsUsed += cl.operandList.size();
                break;

              case BSS:  
                // Write 0s to the bin file for each reserved location
                for (int k = 0; k < cl.operandValue; k++)
                  binFileStream.writeShort(0);
                locationsUsed += cl.operandValue;
                break;

              // Do nothing for these 3 directives
              case ORG:
              case EQU:
              case END:
                break;
              
              default:
                out.println("Assembler internal error in saving the bin file: " +
                  "the directive flag was set but the mnemonic was not a directive.");
                System.exit(1);

            }  // End of switch
            clIndex++;
          }  // End of 'if (!cl.directive)'
        }
      }
    } catch (IOException e)  {
        out.println("I/O Exception occurred:  " + e.getMessage());
        return;
    } finally {
        try  {  
				  binFileStream.close();   // Close the bin file
				} catch (IOException ie)  {
            out.println("Could not close " + binaryFile.getAbsolutePath() + 
                                         ":  " + ie.getMessage());
        }
    }

    // Print the segment info if the flag is set
		if (showSegmentInfo)  {
	    out.println("  " + segmentList.size() + " segment(s)");
	    for (int z = 0; z < segmentList.size(); z++)
	      out.println("  Segment " + (z + 1) + "> " + 
	        ((Segment) segmentList.get(z)).toString());
		}

		// Check to see if any of the segments overlap.
		for (int seg1 = 0; seg1 < segmentList.size() - 1; seg1++)  {
			for (int seg2 = seg1 + 1; seg2 < segmentList.size(); seg2++)  {
			  if (((Segment) segmentList.get(seg1)).isOverlapping(
				                    (Segment) segmentList.get(seg2)))
				  System.out.println("Warning> Segments " + (seg1 + 1) 
					                + " and " + (seg2 + 1) + " overlap.");
			}
		}

    /* Print the symbol table information if the flag is set. */
		if (showSymbolTable)  {
		  int x = 0;
		  String symbolLine = symbolTable.toString(x++);
			if (symbolLine == null) 
			  out.println("  There are no symbols in the symbol table.");
			else  {
			  out.println("  The Symbol Table for " + codeFile.getName() + ":");
				while (symbolLine != null)  {
				  out.println("  " + symbolLine);
					symbolLine = symbolTable.toString(x++);
				}
			}
		}

    out.println("Finished");

  }  // End of run




 /* Assember's first pass.  The following tasks are performed in this pass:
     1) The mnemonic is located 
     2) If a label exists, it is validated and inserted into the symbol table.
     3) All directives are interpretted completely.
        The operand for non-directives is saved without being parsed or 
        evaluated.
     4) All known attributes of the line of code are stored in its CodeLine
        object even if they are not needed later.  This is done so that the 
        attributes of the line of code can be printed out for debugging purposes.
     
     When the END directive is encountered, all the remaining code in the code 
     list is discarded!

     Note that most of the errors found in the first pass are not fatal so 
     the pass can continue.  If the error is fatal i.e. it is unknown how 
     to continue, the assembler quits. 
    
     Good luck trying to figure this code out! */

  private void firstPass()
  {
    // Variables
    int line = 0;                // Index of the current line in tscCode
    int mIndex = 0;              // Index of the mnemonic in the code line
    boolean foundIt = false;     // Set to true when a valid mnemonic is found
    boolean endFound = false;    // true when .END is found
    

    for (line = 0; line < tscCode.size(); line++)  {
      // Get the code line
      CodeLine cl = (CodeLine) tscCode.get(line);
      int mnemonic;              // Index of a mnemonic in mnemonicStrings

      // Search for the mnemonic
      foundIt = false;     // Set to true when a valid mnemonic is found
      for (mnemonic = 0; mnemonic < mnemonicStrings.length - 1; mnemonic++)  {
          // The "-1" above ensurses that "INVALID" is not mistaken for a 
          // valid mnemonic.

        mIndex = cl.code.indexOf(mnemonicStrings[mnemonic]);

        while (mIndex > -1)  {  // Possibly found a mnemonic
  
          // Make sure the mnemonic is not just part of a label. This is
          // done by checking the left and right side of the mnemonic.
          int mLength = mnemonicStrings[mnemonic].length();
          if (((mIndex==0) || (Character.isWhitespace(cl.code.charAt(mIndex-1)))) &&
              ((cl.code.length() == mIndex + mLength) ||
						  (Character.isWhitespace(cl.code.charAt(mIndex + mLength)))))  {

            // FOUND A MNEMONIC!  Save it in codeLine
            foundIt = true;
            cl.mnemonic = mnemonic;

            // Check for a label
            String label = null;
            if (mIndex != 0) {            // Found a label
              label = cl.code.substring(0, mIndex).trim();
              // Remove colon and preceding spaces
              if (label.endsWith(":"))  {
                label = label.substring(0, label.length() - 1).trim();
              }
              // Check for the special case of a colon with no label
              if (label.length() == 0)  {
                label = null;
                error("A label must start with a letter: \":\"", cl);
              }
              else   {  // Make sure label is valid
                if (isLabelValid(label, LABEL, cl))
                  cl.label = label;  // Save label in the code structure
              }
            }

            // Save the portion of the code line after the mnemonic
            if (cl.code.length() > mIndex + mLength)  {
              cl.operandString = cl.code.substring(mIndex + mLength).trim();
            }
  
            mIndex = -1;                        // Break out of while loop
            mnemonic = mnemonicStrings.length;  // Break out of for loop
          }  // End of 'if ()'

          else  // Continue search for mnemonic
            mIndex = cl.code.indexOf(mnemonicStrings[mnemonic], mIndex + 1);
            
        }  // End of 'while'
      }  // End of 'for (mnemonic)' i.e. end of searching for mnemonic in a line



      if (!foundIt)         // No mnemonic was found in the line
        error("Invalid instruction or directive", cl);

      else  {
      /* A mnemonic was found so insert the label (if found) into the 
				 symbol table. Continue parsing the rest of the line ONLY if 
         the mnemonic is a directive.  The remainder of the line for 
         an instruction mnemonic is parsed during the second pass. */

        // The label is handled differently for EQU.
        if ((cl.mnemonic != EQU) &&  (cl.label != null))  {
          if (!symbolTable.insertSymbol(cl.label, segMan.getLocCounter()))
            error("The label \"" + cl.label 
                  + "\" already exists in the symbol table", cl);
        }

        if (cl.mnemonic < ORG)  {  // The mnemonic is an instruction
          // Increment the location counter.
          if (!segMan.updateLocCounter(cl))  {
            error("Memory range exceeded: address = " + (MAXMEMSIZE + 1), cl);
            out.println("  Fatal error");
            return;
          }
        }

        else {    // The mnemonic is a directive or pseudo instruction.
          switch (cl.mnemonic)  {
      
            case ORG:
              cl.directive = true;
              if (cl.label != null)  {
                error("The ORG directive cannot have a label: \"" 
									                           + cl.label + "\"", cl);
              }
              if (cl.operandString == null)  {
                error("The ORG directive must have an operand", cl);
                out.println("  Fatal error");
                return;
              }

              // Evaluate the addrString.
              if (!evaluateExpression(cl.operandString, cl))  {
                out.println("  Fatal error");
                return;
              }

							// Check for negative operand
							if (cl.operandValue < 0)  {
                error("Why is the operand for the ORG directive negative?", cl);
                out.println("  Fatal error");
                return;
              }

              // Update the loc counter
              if (!segMan.updateLocCounter(cl))  {
                error("Memory range exceeded: address = " 
									                     + (MAXMEMSIZE + 1), cl);
                out.println("  Fatal error");
                return;
              }
              break;
  
              
            case END:
              cl.directive = true;
              if (cl.label != null)
                error("The END directive cannot have a label: \"" + 
								                               cl.label + "\"", cl);
              endFound = true;
              
              // Since END signifies the end of the code, erase all code that 
              // may follow it.  This is done so that any literals in the code
              // can be appended to the end of the code list.
              int size = tscCode.size();
              if (line < size - 1)  {
                for (int j = size - 1; j > line; j--)
                  tscCode.removeElementAt(j);
              }
						
						  segMan.updateLocCounter(cl);
						
              line = size;    // End is found so break out of for loop
              break;
  
  
            case EQU:
              cl.directive = true;
              if (cl.label == null)  {
                error("The EQU directive must have a label", cl);
              }
              else  {
                if (cl.operandString == null)  {
                  error("The EQU directive must have an operand", cl);
                }
                else  {
                  if (evaluateExpression(cl.operandString, cl))  {
                    if (!symbolTable.insertSymbol(cl.label, cl.operandValue))
                      error("The label \"" + cl.label + 
                        "\" already exists in the symbol table", cl);
                  }
                }
              }

  					  segMan.updateLocCounter(cl);
						  break;
  
  
            case BSC:
              cl.directive = true;
              if (cl.operandString == null)  {
                error("The BSC directive must have an operand", cl);
              }
              else  {  
                // Parse the operand
								parseList(cl.operandString, cl);

                if (cl.operandList != null)  {
                  if (!segMan.updateLocCounter(cl))  {
                    error("Memory range exceeded: address = " 
											                      + (MAXMEMSIZE + 1), cl);
                    out.println("  Fatal error");
                    return;
                  }

                  // Evaluate each item in the operand
                  for (int j = 0; j < cl.operandList.size(); j++)  {
                    if (evaluateExpression(
											      (String) cl.operandList.get(j), cl))  {
                      // Replace the symbolic address with its actual address
                      cl.operandList.set(j, "" + cl.operandValue);
                    }
                  }
                }
              }
              break;
  
  
            case BSS:
              cl.directive = true;
              if (cl.operandString == null)  {
                error("The BSS directive must have an operand", cl);
              }
              else  {
                if (evaluateExpression(cl.operandString, cl))  {
                  if (cl.operandValue <= 0)  {
                    error("Why is the operand for the BSS directive negative?", cl);
                  }
                  else {
                    if (!segMan.updateLocCounter(cl))  {
                      error("Memory range exceeded: address = " 
												                           + (MAXMEMSIZE + 1), cl);
                      out.println("  Fatal error");
                      return;
                    }
                  }
                }
              }
              break;


					  case LAD:
              cl.pseudoInstr = true;
              if (!segMan.updateLocCounter(cl))  {
                error("Memory range exceeded: address = " + (MAXMEMSIZE + 1), cl);
                out.println("  Fatal error");
                return;
              }
						  break;
						
            default:
						  error("Assembler internal error during the 1st pass: unrecognized"
							  + " mnemonic --> " + mnemonic + ":" + cl.mnemonic, cl);
              
          }  // End of switch statement
        }  // End of 'if (cl.mnemonic < ORG) else'
      }  // End of 'if (!foundIt) else'
    }  // End of 'for (line)' loop  i.e. done searching the file for mnemonics
    
    if (!endFound)  {
      out.println("Error> No END directive was found");
      errorCount++;
      return;
    }

  }  // End firstPass

 


  /* Assember's second pass.  The following tasks are performed in the 
	   second task:
     1) Evaluation of the instruction's operand field.
     2) Insertion of opcode, address, source and destination registers, and 
        immediate value into the instruction format i.e. creation of binary 
        instructions.
     3) All the attributes of the line of code are stored in its CodeLine
        class even if they are not needed later.  This is done so that the 
        attributes of the line of code can be printed out for debugging 
				purposes.
  
     If an error occurs, the pass continues so that multiple errors can be 
		 reported in one swipe.  This may result in the creation of an invalid
		 binary instruction(s) but the .bin file will not be created if there
		 are errors so it doesn't matter. */

  private void secondPass()
  {
  
    for (int line = 0; line < tscCode.size(); line++)  {

      // Get the code line object
      CodeLine cl = (CodeLine) tscCode.get(line);
			int rs = 0;
			int rt = 0;
			int rd = 0;
			int immediate = 0;
			int target = 0;

      // Check for an operand and parse it
			if (!cl.directive)   {
				if (cl.operandString != null)  {
				  parseList(cl.operandString, cl);

          if ((numOfOperands[cl.mnemonic] != -1) && (cl.operandList != null)
					         && (cl.operandList.size() != numOfOperands[cl.mnemonic])) 
					{
            error("The " + mnemonicStrings[cl.mnemonic] + " instruction must"
					        + " have " + numOfOperands[cl.mnemonic] + " operand(s): \"" 
							                                + cl.operandString + "\"", cl);
				  }
				}
				else  {
					if ((cl.mnemonic != HLT) && (cl.mnemonic != ENI) 
						                      && (cl.mnemonic != DSI))
            error("The " + mnemonicStrings[cl.mnemonic] + " instruction must"
					    + " have " + numOfOperands[cl.mnemonic] + " operand(s): \"", cl); 
				}
      }
			

			// If no errors found in operandString, evaluate the operands
      if (!cl.error && cl.pseudoInstr)  {

        switch (cl.mnemonic)  {
	
          /**** The Pseudo-Instructions ****/
		
		      /* When pseudo-instructions are evaluated, the appropriate 
					   real instruction(s) is/are inserted into the code list.
						 The pseudo-instructions are not in the instruction switch 
						 statement (found further below) because they are turned
						 into real instructions which are then evaluated in the
						 instruction switch statement.  */

          case LAD:
					  rt = registerNumber((String) cl.operandList.get(0), cl);
            if (evaluateExpression((String) cl.operandList.get(1), cl))  {
						  immediate = cl.operandValue;

							/* LAD translates into two instructions, LHI followed by an 
							ORI.  The LHI instruction info is placed in the LAD codeline 
	  				  object and is evaluated in the instruction switch statement 
							during this loop.  The ORI info is put into a new codeline 
							object and inserted into the code list. It is evaluated the 
							next time through the loop.  */

							CodeLine oriCL = new CodeLine(cl.originalCode);
							oriCL.originalCode = oriCL.originalCode + "  ;ORI instruction";
							oriCL.lineNumber = cl.lineNumber;
							oriCL.address = cl.address + 1;
							oriCL.mnemonic = ORI;
							oriCL.operandString = cl.operandList.get(0) + ", " +
							  cl.operandList.get(0) + ", " + (immediate & LOWHALFMASK);
							oriCL.error = cl.error;
							oriCL.errorMessage = cl.errorMessage;
							tscCode.add(line + 1, oriCL);  // Insert ORI into the code list

              cl.originalCode = cl.originalCode + "  ;LHI instruction";
							cl.mnemonic = LHI;
							cl.pseudoInstr = false;
							cl.operandList.set(1, Integer.toString(
							                      (immediate & HIGHHALFMASK) >> 8));
							cl.operandString = cl.operandList.get(0) + ", " +
							                            cl.operandList.get(1);
            }
						break;

          default:
					  error("Assembler internal error during the 2nd pass: mnemonic "
						        + " is tagged as a pseudo-instruction but is not one: " 
							                                          + cl.mnemonic, cl);
              
				} // End of switch (cl.mnemonic)
			} // End of if (!cl.error)


      if (!cl.error)  {

        // Now evaluate the instructions
        switch (cl.mnemonic)  {

					/**** The Directives ****/
	
	        // ORG, END, BSS, BSC, and EQU were completly evaluated in pass 1
	        // so they are ignored in pass 2.
	        case ORG:
	        case BSS:
	        case BSC:
	        case EQU:
	          break;
	
	        case END:
	          // No more code after .END so exit the 2nd pass.
	          return;     


          /**** The Instructions ****/
	
					// The R-type instructions.
				  case HLT:
	        case ENI:       // These instructions don't have operands
	        case DSI:
	          break;
	
	        case ADD:
	        case SUB:
	        case AND:
				  case ORR:
					  rd = registerNumber((String) cl.operandList.get(0), cl);
					  rs = registerNumber((String) cl.operandList.get(1), cl);
  				  rt = registerNumber((String) cl.operandList.get(2), cl);
						break;

          case NOT:
					case TCP:
					case SHL:
					case SHR:
					  rd = registerNumber((String) cl.operandList.get(0), cl);
					  rs = registerNumber((String) cl.operandList.get(1), cl);
  					break;

					case WWD:
					case JPR:
					case JRL:
					  rs = registerNumber((String) cl.operandList.get(0), cl);
						break;

					case RWD:
					  rd = registerNumber((String) cl.operandList.get(0), cl);
						break;

					// The I-type instructions
					case ADI:
					  rt = registerNumber((String) cl.operandList.get(0), cl);
					  rs = registerNumber((String) cl.operandList.get(1), cl);
            if (evaluateExpression((String) cl.operandList.get(2), cl))  {
						  immediate = cl.operandValue;
							signedOffsetWarning(immediate, cl);
						}
						break;

					case ORI:
					  rt = registerNumber((String) cl.operandList.get(0), cl);
					  rs = registerNumber((String) cl.operandList.get(1), cl);
            if (evaluateExpression((String) cl.operandList.get(2), cl))  {
						  immediate = cl.operandValue;
						  if (immediate < 0)
							  out.println("Warning at line " + cl.lineNumber + "> A "
								  + "negative immediate value for ORI may cause unexpected"
									                             + " results: " + immediate);
							else if (immediate > 0xff)
						    out.println("Warning at line " + cl.lineNumber + "> The"
								            + " binary representation of " + immediate + 
									                        " requires more than 8 bits.");
						}
						break;

					case LHI:
					  rt = registerNumber((String) cl.operandList.get(0), cl);
            if (evaluateExpression((String) cl.operandList.get(1), cl))  {
						  immediate = cl.operandValue;
						  if (immediate < 0)
							  out.println("Warning at line " + cl.lineNumber + "> A "
								  + "negative immediate value for LHI may cause unexpected"
									                             + " results: " + immediate);
							else if (immediate > 0xff)
						    out.println("Warning at line " + cl.lineNumber + "> The"
								            + " binary representation of " + immediate + 
									                        " requires more than 8 bits.");
						}
						break;

					case LWD:
					case SWD:
					  rt = registerNumber((String) cl.operandList.get(0), cl);
					  rs = registerNumber((String) cl.operandList.get(1), cl);
            if (evaluateExpression((String) cl.operandList.get(2), cl))  {
						  immediate = cl.operandValue;
							signedOffsetWarning(immediate, cl);
						}
						break;

          case BNE:
					case BEQ:
					case BGZ:
					case BLZ:
					  rs = registerNumber((String) cl.operandList.get(0), cl);
						String offsetString;
					  if ((cl.mnemonic == BNE) || (cl.mnemonic == BEQ))  {
							rt = registerNumber((String) cl.operandList.get(1), cl);
					    offsetString = (String) cl.operandList.get(2);
						}
						else
					    offsetString = (String) cl.operandList.get(1);

						/* If the offset field in a branch instruction is a label, 
						   the difference between the address of the instruction after 
							 the branch and the value of the label is computed.  This 
							 offset is inserted into the binary instruction.  If the 
							 field contains a number, the number is inserted into the 
							 binary instruction.  The offset field of a branch 
							 instruction canNOT contain an expression.  */

						if (Tools.isNumber(offsetString))  {      // Its a number
				      immediate = Tools.stringToInt(offsetString);
				    }
				    else {                                    // Its a symbol
				      if (isLabelValid(offsetString, SYM_ADDR, cl))  {
				        if (!symbolTable.isSymbolInTable(offsetString))
				          error("The symbolic address \"" + offsetString 
									        + "\" is not in the symbol table", cl);
				        else  {
				          immediate = symbolTable.getAddress(offsetString);
									
									// The EQU directive can make a symbol value negative 
									// so check the sign of the value.
									if (immediate < 0)
									  error("The value of \"" + offsetString + "\" is " +
										       immediate + ".  Cannot branch to a negative" 
											                              + " address.", cl);
									immediate = immediate - (cl.address + 1);
								}
				      }
				    }
            cl.operandValue = immediate;
						signedOffsetWarning(immediate, cl);
						break;

					// The J-type instructions
					case JMP:
					case JAL:
            if (evaluateExpression((String) cl.operandList.get(0), cl))  {
						  target = cl.operandValue;
							if (target < 0)
							  error("Can't jump to a negative address: " + target, cl);
						  if ((target & 0x0000f000) != (cl.address & 0x0000f000))  {
						    out.print("Warning at line " + cl.lineNumber);
								out.println("> This jump crosses a memory page boundary");
								out.print("  Consider using JPR or JRL instead of ");
								out.println(mnemonicStrings[cl.mnemonic] + ".");
							}
						}
  					break;

	        default:
	          out.println("Assembler internal error during the 2nd pass: " 
	                      + "the mnemonic " + cl.mnemonic + " is not valid.");
					  errorCount++;
					  break;
	
	      }  // End of switch statement


				// Insert the opcode and all fields into the binary instruction.
				if ((!cl.directive) && (!cl.error))  {
          cl.binaryInst = (short) opcodes[cl.mnemonic];
				  cl.binaryInst = (short) (cl.binaryInst | (rd << 6));
				  cl.binaryInst = (short) (cl.binaryInst | (rt << 8));
				  cl.binaryInst = (short) (cl.binaryInst | (rs << 10));
				  cl.binaryInst = (short) (cl.binaryInst | (immediate & IMMEDMASK));
				  cl.binaryInst = (short) (cl.binaryInst | (target & TARGETMASK));
			  }
	          
			}  // End of if (!cl.error)
    }  // End of 'for' loop
  }  // End of secondPass




  /* This code is in its own method because it is repeated many times
     during the assembly process. */
  public void error(String message, CodeLine cl)
  {
    out.println("Error at line " + cl.lineNumber + "> " + message);
    errorCount++;

    // Save the state for debugging purposes
    cl.error = true;
    cl.errorMessage = message;
  }  // End of error




	/* Used by instructions with 8 bit signed immediate fields. */
	private void signedOffsetWarning(int offset, CodeLine cl)
	{
	  if ((offset < -128) || (offset > 127))
	    out.println("Warning at line " + cl.lineNumber + "> The binary " +
			    "representation of " + offset + " requires more than 7 bits.");
	}




	/* Returns the register number if the first character in 'reg' 
	   is $ and the second character is a number between 0 and 3.  
		 Otherwise, an error message is printed and -1 is returned.
		 'reg' must not be null or empty.  */

  private int registerNumber(String reg, CodeLine cl)  
	{
	  reg = reg.trim();
		if ((reg.charAt(0) != '$') || (reg.length() == 1)
		           || (!Tools.isNumber(reg.substring(1))))  {
		  error("Invalid register: " + reg, cl);
			return -1;
		}
		
		int regNumber = Tools.stringToInt(reg.substring(1));
		if ((regNumber < 0) || (regNumber > 3))  {
		  error("Invalid register number: " + reg, cl);
		  return -1;
		}

	  return regNumber;
		
	}  // End of isRegister




  /* This method is used to determine if a label is valid or to see if 
     an operand is a valid symbolic address.  It returns true if the 
     label or symbol is valid.  Otherwise, the error method is called 
     and false is returned.  The label's whitespace must be trimmed 
     before calling this method and the label cannot be empty.  */

  private boolean isLabelValid(String tempLabel, boolean isLabel, CodeLine cl)
  {
    int i = 0;
    String type = "symbolic address";
    
    if (isLabel)  type = "label";

    // Make sure the label starts with a letter
    if (!Character.isLetter(tempLabel.charAt(0)))  {
      error("A " + type + " must start with a letter: \"" + tempLabel + "\"", cl);
      return false;
    }
      
    // Make sure the label is one alphanumeric word
    for (i = 0; i < tempLabel.length(); i++)
      if (!Character.isLetterOrDigit(tempLabel.charAt(i)))  {
        error("A " + type + " can only consist of alphanumeric characters: \""
                                                         + tempLabel + "\"", cl);
        return false;
      }

    // Make sure the label is not a keyword
    for (i = 0; i < mnemonicStrings.length - 1; i++)
      if (tempLabel.equals(mnemonicStrings[i]))  {
        error("A " + type + " cannot be a keyword: \"" + tempLabel + "\"", cl);
        return false;
    }

    return true;
  }  // End of isLabelValid




  /*  Used to parse the operand of some directives and instructions.
      Parses a comma delimeted list and stores the tokens in a 
      vector.  The token vector is saved in the code line class. All
			tokens are trimmed of whitespace before being put in the list.
      Returns true if no errors occurred during parsing.  There 
      must be an item before and after every comma or false is 
      returned.  No checks are made to make sure the symbols or 
      numbers are valid. */

  private boolean parseList(String tokenList, CodeLine cl)
  {
    if (tokenList == null)        // List is null so return
      return false;

    int x = 0;
    Vector tokens = new Vector(3, 1);
    String list = tokenList.trim();

    if (list.length() == 0)        // List is empty so return
      return true;

    x = list.indexOf(',');
    if (x == -1)   {               // Only one value in the list
      tokens.addElement(list);
      cl.operandList = tokens;
      return true;
    }

    while (x > -1)  {
      // Check to see if there is a value\label before and after the comma
      if ((x == 0) || (x == list.length() - 1))  {
        error("Missing an item before and/or after a comma", cl);
        return false;
      }
      else  {
        tokens.addElement(list.substring(0, x).trim());
        list = list.substring(x + 1).trim();
      }
      x = list.indexOf(',');
    }
    tokens.addElement(list);  // Add last element
    
    cl.operandList = tokens;
    return true;

  }  // End of parseList




  /*  Used to evaluate all non-register operand fields.  The 
      operand field can consist of, at most, one operator and 
      two operands.  The operands are validated to see if they 
      are numbers or valid symbols.  The expression is then 
      evaluated and the result is stored in cl.operandValue.  */
  private boolean evaluateExpression(String expression, CodeLine cl)
  {
    // Make sure the expression is not null
    if (expression == null)
      return false;

    String expr = expression.trim();
    
    // Make sure the expression is not empty
    if (expr.length() == 0)
      return false;

    /* Search for a +, -, *, or /.  If one is found, save the characters
       before it as the first operand and the characters after it as the 
       second operand.  Then evaluate the expression.
       Note that i starts at 1 instead of 0 because the character at the 
       0th index could be a negative sign for a literal, in which case 
       it should not be mistaken for a minus sign.  */
    for (int i = 1; i < expr.length(); i++)  {
      char op = expr.charAt(i);
      if ((op == '+') || (op == '-') || (op == '*') || (op == '/')) { 
        // Found operator
        if (i == expr.length() - 1)  {
          error("Missing an operand after the operator", cl);
          return false;
        }
        else  {
          // Get the operands
          String firstOperand = expr.substring(0, i).trim();
          String secondOperand = expr.substring(i + 1).trim();
          
          // Evaluate the first operand
          int firstOperandValue = 0;
          if (Tools.isNumber(firstOperand))  {   // Its a number
            firstOperandValue = Tools.stringToInt(firstOperand);
          }
          else {                                 // Its a symbol
            if (!isLabelValid(firstOperand, SYM_ADDR, cl))
              return false;
            else  {
              if (!symbolTable.isSymbolInTable(firstOperand))  {
                error("The symbolic address \"" + firstOperand 
                         + "\" is not in the symbol table", cl);
                return false;
              }
              else
                firstOperandValue = symbolTable.getAddress(firstOperand);
            }
          }
            
          // Evaluate the second operand
          int secondOperandValue = 0;
          if (Tools.isNumber(secondOperand))  {   // Its a number
            secondOperandValue = Tools.stringToInt(secondOperand);
          }
          else {                                  // Its a symbol
            if (!isLabelValid(secondOperand, SYM_ADDR, cl))
              return false;
            else  {
              if (!symbolTable.isSymbolInTable(secondOperand))  {
                error("The symbolic address \"" + secondOperand 
                         + "\" is not in the symbol table", cl);
                return false;
              }
              else
                secondOperandValue = symbolTable.getAddress(secondOperand);
            }
          }
          
          // Evaluate the expression
          int result = 0;
          switch (op)  {
            case '+':
              result = firstOperandValue + secondOperandValue;
              break;
            case '-':
              result = firstOperandValue - secondOperandValue;
              break;
            case '*':
              result = firstOperandValue * secondOperandValue;
              break;
            case '/':
              result = firstOperandValue / secondOperandValue;
              break;
            default:
              out.println("Assembler internal error in evaluateExpression:" 
							                          + op + " is not a valid operator.");
              System.exit(1);
          }
          
          // Save the result
          cl.operandValue = result;
          return true;
        }
      }
    }  // End of 'for' loop i.e. end of searching for an operator
    

    // Didn't find an operator so just evaluate expression as a
    // single operand.
    if (Tools.isNumber(expr))  {      // Its a number
      cl.operandValue = Tools.stringToInt(expr);
    }
    else {                                 // Its a symbol
      if (!isLabelValid(expr, SYM_ADDR, cl))
        return false;
      else  {
        if (!symbolTable.isSymbolInTable(expr))  {
          error("The symbolic address \"" + expr + "\" is not in the " +
                                                     "symbol table", cl);
          return false;
        }
        else
          cl.operandValue = symbolTable.getAddress(expr);
      }
    }
    
    return true;
          
  }  // End of evaluateExpression

}  // End of Assembler class

