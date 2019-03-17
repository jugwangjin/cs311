/*
© 2000, Performance Evaluation Laboratory, Brigham Young University. Reproduction of all or part of this work is permitted for non-profit educational
or research use provided this copyright notice remains intact. All other rights reserved. 
*/

import java.util.Vector;

/* This class stores a single line of TSC code and all 
   the attributes for the line.  Some of the attributes 
	 are stored here only for debugging purposes. 
		
	 All comments and beginning and ending whitespace are removed.  */
	
class CodeLine
{
	String originalCode = null;  // Original code w/ case preserved and comments removed
  String code = null;          // Contains the upper case version of 'originalCode'
	int lineNumber = 0;          // The codes line number in the .tsc file
	int address = 0;             // The memory address of the binary instruction
	int mnemonic = Assembler.INVALID; // The mnemonic's index in Assembler.mnemonicStrings[]
	String label = null;         // The label (if any)
	String operandString = null; // Holds the operand field before parsing
	int operandValue = 0;        // Temporary holder for a field in the operand
	Vector operandList = null;   // Holds the elements of the parsed operandString
	boolean directive = false;   // True if code is a directive
	boolean pseudoInstr = false; // True if code is a pseudo-instruction
	short binaryInst = 0;        // THE binary instruction
	boolean error = false;       // True if there was a problem with the code line
	String errorMessage = "";    // Description of the error (if any)



/* It is assumed that the line of code is not empty or 
	 null and that the entire line is not a comment.  */
	public CodeLine(String code)
	{
    // Erase comment (if any)
    int x = code.indexOf(';');
		if (x > -1)
		  code = code.substring(0, x).trim();
		else
		  code = code.trim();

		this.originalCode = code;
		this.code = code.toUpperCase();

	}  // End of constructor

}  // End of CodeLine class

