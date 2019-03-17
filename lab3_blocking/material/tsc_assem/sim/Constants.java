/*
© 2000, Performance Evaluation Laboratory, Brigham Young University. Reproduction of all or part of this work is permitted for non-profit educational
or research use provided this copyright notice remains intact. All other rights reserved. 
*/

import java.awt.Insets;
import java.awt.Color;
import java.awt.Font;
import javax.swing.ImageIcon;


/* Constants that are used by more than one class. */


final class Constants
{
  // Memory highlighting
	final static int CURRENT_INST = 101;  
  final static int CURRENT_DATA = 102;
  final static int NO_HIGHLIGHT = 103;
  
	// Memory access type
	final static int DATA = 151;
	final static int INSTRUCTION = 152;
	final static int CONSOLE_ACCESS = 153;
  
	// String number formats
	final static int HEX = 201;
	final static int DECIMAL = 202;
	final static int SIGNED_DECIMAL = 203;
	final static int BINARY = 204;
	final static int NONE = 205;
	
	// I/O type
	final static int INPUT = 351;
	final static int OUTPUT = 352;
	
	// Input sources and output destinations
	final static int CONSOLE = 360;
	final static int FILE = 361;
	final static int FILE_ERROR = 362;
  
  // Style and size of text fields
	final static String FONTNAME = "Monospaced";
  final static int FONTSTYLE = Font.PLAIN;
	final static int FONTSIZE = 13;
	final static Font FONT = new Font(FONTNAME, FONTSTYLE, FONTSIZE);

	/* Registers (indices to the regs array). The general purpose 
	   registers must start at 0 to be used properly by the sim.
		 All register numbers must be consecutive. */
  final static int NUMREGS = 5;
	final static int GPR0 = 0;
	final static int GPR1 = 1;
	final static int GPR2 = 2;
	final static int GPR3 = 3;
	final static int PC = 4;
	
	// Magic numbers for different types of files
  final static int ROM_MAGIC_NUMBER = 0x12feed34;
  
	// Various icons
  final static ImageIcon yellowLightOnImage = new ImageIcon("images/yellow-on.gif");
  final static ImageIcon yellowLightOffImage = new ImageIcon("images/yellow-off.gif");
  final static ImageIcon greenLightOnImage = new ImageIcon("images/green-on-small.gif");
  final static ImageIcon greenLightOffImage = new ImageIcon("images/green-off-small.gif");
  final static ImageIcon redLightOnImage = new ImageIcon("images/red-on-small.gif");
  final static ImageIcon redLightOffImage = new ImageIcon("images/red-off-small.gif");
	
	// Colors
	final static Color BORING_GRAY_1 = new Color(223, 223, 223);
	final static Color BORING_GRAY_2 = new Color(215, 215, 215);
	final static Color TEXT_BACKGROUND = new Color(232, 232, 232);
  final static Color BLACK = Color.black;

	// Misc.
	final static String NEWLINE = System.getProperty("line.separator");
	final static Insets ZERO_INSET = new Insets(0, 0, 0, 0);
	
	
}  // End of Constants class

