/*
© 2000, Performance Evaluation Laboratory, Brigham Young University. Reproduction of all or part of this work is permitted for non-profit educational
or research use provided this copyright notice remains intact. All other rights reserved. 
*/

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

	
/* This class is used to redirect an output stream to a dedicated 
   "console" window.  If the window is not shown, it is created 
	 and displayed before the data is printed.  
		
	 This class is designed to print unexpected error output.  The 
	 window is destroyed every time it is closed because it is not 
	 expected to be used very often.  */

class ConsoleOutputStream extends OutputStream
{
	
	private JFrame console;
	private JTextArea outputArea;
	private String consoleTitle = " ";




	public ConsoleOutputStream(String consoleTitle)
	{
	  setConsoleTitle(consoleTitle);
	}  // End of constructor
	
	
	
	
	/* Appends a character to the output area of the window. 
	   Overwrites the method in OutputStream.  If the console 
		 is not open*/
	public void write(int b) throws IOException
	{
	  if (console == null)  createConsole();
	  outputArea.append(String.valueOf((char) b));
	}  // End of write
	
	
	
	
	/* Appends a string to the output area of the window.
	   Overwrites the method in OutputStream.  */
	public void write(byte[] b) throws IOException
	{
	  if (console == null)  createConsole();
	  outputArea.append(new String(b));
	}  // End of write
	
	
	
	
	/* Appends part of a string to the output area of the window.
	   Overwrites the method in OutputStream.  */
	public void write(byte b[], int off, int len) throws IOException
	{
	  if (console == null)  createConsole();
	  outputArea.append(new String(b, off, len));
	}  // End of write(,,)




	/* Create the console window. */
	private void createConsole()
	{
	  console = new JFrame(consoleTitle);
		console.setDefaultCloseOperation(console.DO_NOTHING_ON_CLOSE);
		
		// Respond to WindowClosing event
		console.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        console.dispose();
				// Set console to null so it will be garbage collected
				outputArea = null;
				console = null;
				System.gc();
      }
	  });

    // Create the output area
		outputArea = new JTextArea(24, 90);
		outputArea.setEditable(false);
		outputArea.setFont(new Font("Monospaced", 0, 12));
		outputArea.setMargin(new Insets(0, 8, 0, 0));
		outputArea.setBackground(Color.black);
		outputArea.setForeground(Color.white);
		console.getContentPane().add(new JScrollPane(outputArea),
		                                     BorderLayout.CENTER);

		console.pack();

    // Position the console in the center of the screen
		Dimension screenSize = console.getToolkit().getScreenSize();
		Dimension consoleSize = console.getSize();
		console.setLocation((screenSize.width - consoleSize.width) / 2,
		                    (screenSize.height - consoleSize.height) / 2);

		// Show the console
		console.show();
		
	}  // End of createConsole




  /* Set the console title.  A null string will cause the title 
	   to be erased. */
	public void setConsoleTitle(String consoleTitle)
	{
	  if (consoleTitle == null)
		  this.consoleTitle = " ";
		else
		  this.consoleTitle = consoleTitle;
	}  // End of setConsoleTitle


}  // End of ConsoleOutputStream class
	

