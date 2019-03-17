/*
© 2000, Performance Evaluation Laboratory, Brigham Young University. Reproduction of all or part of this work is permitted for non-profit educational
or research use provided this copyright notice remains intact. All other rights reserved. 
*/

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

class Calc extends JFrame implements ActionListener, ListDataListener
{
	//operator codes
	final static int ADD = 1;
	final static int SUB = 2;
	final static int MULT = 4;
	final static int DIVIDE = 8;
	
	public Memory mem;
	public Display display;	
	
	final static int DATA = Constants.DATA;	
	
	//calc buttons
	private JButton numButton1;
	private JButton numButton2;
	private JButton numButton3;
	private JButton numButton4;
	private JButton numButton5;
	private JButton numButton6;
	private JButton numButton7;
	private JButton numButton8;
	private JButton numButton9;
	private JButton numButton0;
	
	private JButton plusButton;
	private JButton minusButton;
	private JButton multButton;
	private JButton divideButton;
	private JButton equalsButton;
	private JButton onOffButton;
	private JButton clearAllButton;
	private JButton clearEntryButton;
	private JButton posNegButton;
	
	//calc display
	protected JTextField calcDisplay;	
	public String displayString;
	
	//max number in two's complement 8 bit num is 32767
	//therefore five digits is max length of a number.
	private int maxStringLength = 5;
	
	//reserved memory for calc i/o (calc's registers)
	private int RIGHTOPERANDREG = 61167;
	private int LEFTOPERANDREG = 61166;
	private int OPERATORREG = 61168;
	protected int DATASENTREG = 61170;
	protected int ANSWERREG = 61169;
	private int DONEREG = 61171;
	
	//current state of calc - initialized to zero,
	//1 after operator hit, 2 when second number entered,
	//3 when answer is displayed on the display of calc.
	protected int stateOfCalc = 0;        
	
	//true if posative or zero, false if negitive
	protected boolean signState = true;
	
	//true if on, false if off
	protected boolean powerState = false;
	
	private GridBagLayout gridBag = new GridBagLayout();
	private GridBagConstraints gbc = new GridBagConstraints();
	private Container contentPane = getContentPane();

  public Calc(Memory mem, Display display)
  {		
		this.mem = mem;			
		this.display = display;
		//initialize display string
		displayString = "0";		
	
		//Initialize the reserved memory
		resetMem();
		
    // Initialize the frame.
    contentPane.setLayout(gridBag);
		
		createDisplay();

		setAllEnabled(false);   //initially the calc is off
		display.calcRunning = true;
		
		//dispose of window and reset the memory when the window is closed
		addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
				 resetMem();
				 turnOff();
			   dispose();
      }
	  });
		mem.addListDataListener(this);
	}
	
	private void createDisplay(){		
	
		//creates calc display - called from the constructor
	
		setResizable(false);
		
		Insets gbcInset = new Insets(2, 2, 2, 2);
		gbc.insets = gbcInset;
		
		calcDisplay = new JTextField(15);
		calcDisplay.setHorizontalAlignment(JTextField.RIGHT);
		calcDisplay.setBorder(BorderFactory.createLoweredBevelBorder());
		calcDisplay.setBackground(Constants.TEXT_BACKGROUND);
		calcDisplay.setEditable(false);
		gbcInset.right = 7;
		gbcInset.left = 7;
		gbc.anchor = gbc.EAST;
		gbc.fill = gbc.HORIZONTAL;
		gbc.gridwidth = 7;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gridBag.setConstraints(calcDisplay, gbc);		
		contentPane.add(calcDisplay, gbc);
	  calcDisplay.setText(displayString);
		
		numButton7 = new JButton("7");
		numButton7.addActionListener(this);
		gbc.anchor = gbc.CENTER;
		gbc.gridwidth = 1;
		gbcInset.left = 7;		
		gbcInset.right = 2;
		gbcInset.top = 7;
    gbc.gridx = 0;
    gbc.gridy = 1;
    gridBag.setConstraints(numButton7, gbc);
    contentPane.add(numButton7, gbc);
		
		numButton4 = new JButton("4");		
		numButton4.addActionListener(this);
		gbcInset.top = 2;
    gbc.gridx = 0;
    gbc.gridy = 2;
    gridBag.setConstraints(numButton4, gbc);
    contentPane.add(numButton4, gbc);
		
		numButton1 = new JButton("1");	
		numButton1.addActionListener(this);
		gbcInset.bottom = 2;
    gbc.gridx = 0;
    gbc.gridy = 3;
    gridBag.setConstraints(numButton1, gbc);
    contentPane.add(numButton1, gbc);
		
		numButton8 = new JButton("8");
		numButton8.addActionListener(this);
		gbcInset.top = 7;
		gbcInset.bottom = 2;
		gbcInset.left = 2;
		gbc.gridx = 1;
    gbc.gridy = 1;
    gridBag.setConstraints(numButton8, gbc);
    contentPane.add(numButton8, gbc);
		
		numButton9 = new JButton("9");	
		numButton9.addActionListener(this);
		gbcInset.right = 2;	
    gbc.gridx = 2;
    gbc.gridy = 1;
    gridBag.setConstraints(numButton9, gbc);
    contentPane.add(numButton9, gbc);
		
		numButton3 = new JButton("3");	
		numButton3.addActionListener(this);
		gbcInset.top = 2;
		gbcInset.bottom = 2;
    gbc.gridx = 2;
    gbc.gridy = 3;
    gridBag.setConstraints(numButton3, gbc);
    contentPane.add(numButton3, gbc);

		numButton6 = new JButton("6");		
		numButton6.addActionListener(this);
		gbcInset.bottom = 2;
    gbc.gridx = 2;
    gbc.gridy = 2;
    gridBag.setConstraints(numButton6, gbc);
    contentPane.add(numButton6, gbc);

		numButton5 = new JButton("5");
		numButton5.addActionListener(this);
		gbcInset.right = 2;				
		gbcInset.left = 2;		
    gbc.gridx = 1;
    gbc.gridy = 2;
    gridBag.setConstraints(numButton5, gbc);
    contentPane.add(numButton5, gbc);		
		
		numButton2 = new JButton("2");		
		numButton2.addActionListener(this);
		gbcInset.bottom = 2;
		gbcInset.left = 2;		
    gbc.gridx = 1;
    gbc.gridy = 3;
    gridBag.setConstraints(numButton2, gbc);
    contentPane.add(numButton2, gbc);
		
		numButton0 = new JButton("0");
		numButton0.addActionListener(this);
		gbcInset.bottom = 7;
		gbcInset.left = 7;
		gbc.gridx = 0;
		gbc.gridy = 4;
		gridBag.setConstraints(numButton0, gbc);
		contentPane.add(numButton0, gbc);
		
		divideButton = new JButton("/");	
		divideButton.setEnabled(false);
		divideButton.addActionListener(this);
		gbc.anchor = gbc.WEST;
		gbc.fill = gbc.HORIZONTAL;
		gbc.gridwidth = gbc.REMAINDER;
		gbcInset.top = 7;	
		gbcInset.left = 2;
		gbcInset.bottom = 2;
		gbcInset.right = 7;
    gbc.gridx = 3;
    gbc.gridy = 1;
    gridBag.setConstraints(divideButton, gbc);
    contentPane.add(divideButton, gbc);
		
		plusButton = new JButton("+");	
		plusButton.setEnabled(false);
		plusButton.addActionListener(this);
		gbcInset.top = 2;
		gbcInset.bottom = 7;
		gbcInset.right = 7;		
    gbc.gridx = 3;
    gbc.gridy = 4;
    gridBag.setConstraints(plusButton, gbc);
    contentPane.add(plusButton, gbc);
		
		minusButton = new JButton("-");
		minusButton.setEnabled(false);
		minusButton.addActionListener(this);
		gbc.gridwidth = gbc.REMAINDER;
		gbcInset.bottom = 2;
		gbcInset.top = 2;		
    gbc.gridx = 3;
    gbc.gridy = 3;
    gridBag.setConstraints(minusButton, gbc);
    contentPane.add(minusButton, gbc);
		
		multButton = new JButton("*");
		multButton.setEnabled(false);
		multButton.addActionListener(this);
    gbc.gridx = 3;
    gbc.gridy = 2;
    gridBag.setConstraints(multButton, gbc);
    contentPane.add(multButton, gbc);
		
		equalsButton = new JButton("=");		
		equalsButton.addActionListener(this);
		gbcInset.bottom = 7;
		gbcInset.right = 2;
		gbc.gridx = 1;
		gbc.gridy = 4;
		gbc.gridwidth = 2;
		gridBag.setConstraints(equalsButton, gbc);
		contentPane.add(equalsButton, gbc);
		
		onOffButton = new JButton("On/Off");
		onOffButton.addActionListener(this);
		gbcInset.right = 7;
		gbc.gridx = 3;
		gbc.gridy = 5;
		gridBag.setConstraints(onOffButton, gbc);
		contentPane.add(onOffButton, gbc);
		
		clearEntryButton = new JButton("CE");
		clearEntryButton.addActionListener(this);
		gbcInset.right = 2;
		gbc.gridx = 1;
		gbc.gridy =5;
		gbc.gridwidth = 1;
		gridBag.setConstraints(clearEntryButton, gbc);
		contentPane.add(clearEntryButton, gbc);
		
		posNegButton = new JButton("+/-");
		posNegButton.addActionListener(this);
		gbcInset.right = 2;
		gbc.gridx = 2;
		gbc.gridy =5;
		gbc.gridwidth = 1;
		gridBag.setConstraints(posNegButton, gbc);
		contentPane.add(posNegButton, gbc);
		
		clearAllButton = new JButton("C");
		clearAllButton.addActionListener(this);
		gbcInset.left = 7;
		gbc.gridx = 0;
		gbc.gridy = 5;
		gbc.gridwidth = 1;
		gridBag.setConstraints(clearAllButton, gbc);
		contentPane.add(clearAllButton, gbc);		
	}//createDisplay
	
	public void setEnabledButtons(boolean enable)
	{
		//enables/disables all the operation buttons and equals button
		
    multButton.setEnabled(enable); 
		divideButton.setEnabled(enable);
		plusButton.setEnabled(enable);
		minusButton.setEnabled(enable);
		equalsButton.setEnabled(enable);
		clearAllButton.setEnabled(enable);
		clearEntryButton.setEnabled(enable);		
	} // End of setEnabledButtons

	public void setAllEnabled(boolean enable)
	{
		//enables or disables everything on the display except for the on/off switch
		numButton1.setEnabled(enable);
		numButton2.setEnabled(enable);
		numButton3.setEnabled(enable);
 		numButton4.setEnabled(enable);
	 	numButton5.setEnabled(enable);
	 	numButton6.setEnabled(enable);
		numButton7.setEnabled(enable);
	 	numButton8.setEnabled(enable);
		numButton9.setEnabled(enable);
		numButton0.setEnabled(enable);
	
		plusButton.setEnabled(enable);
		minusButton.setEnabled(enable);
		multButton.setEnabled(enable);
		divideButton.setEnabled(enable);
		equalsButton.setEnabled(enable);
		clearAllButton.setEnabled(enable);
		clearEntryButton.setEnabled(enable);
		posNegButton.setEnabled(enable);
		
		calcDisplay.setEnabled(enable);	 
		
	} // End of setAllEnabled

	
	// Responds to all button events on the TSC Caclculator
  public void actionPerformed(ActionEvent e)
	{
		String ac = e.getActionCommand();
	 
		if(displayString.length()<maxStringLength || stateOfCalc==3 || stateOfCalc ==1)
		{
			if(ac == "1")
			{
				if (stateOfCalc == 3)
				{
					displayString = ac;
					stateOfCalc =0;
					signState = true;
				}			
			else if(stateOfCalc == 1)
				{
					displayString = ac;
					stateOfCalc =2;		
					signState = true;
				}
			else if(displayString == "0")	{
					signState = true;
					displayString = ac;
				}
			else
					displayString = displayString + ac;
			calcDisplay.setText(displayString);				
			}
			else if(ac == "2")
			{
				if (stateOfCalc == 3)
				{
					displayString = ac;
					stateOfCalc =0;
					signState = true;
				}
				else if(stateOfCalc == 1)
				{
					displayString = ac;
					stateOfCalc =2;	
					signState = true;
				}
					else if(displayString == "0")	{
					signState = true;
					displayString = ac;
				}
				else
					displayString = displayString + ac;
			calcDisplay.setText(displayString);
			}
			else if(ac == "3")
			{
				if (stateOfCalc == 3)
				{
					displayString = ac;
					stateOfCalc =0;
					signState = true;
				}
				else if(stateOfCalc == 1)
				{
					displayString = ac;
					stateOfCalc =2;		
					signState = true;
				}
					else if(displayString == "0")	{
					signState = true;
					displayString = ac;
				}
				else
					displayString = displayString + ac;
				calcDisplay.setText(displayString);
			}
			else if(ac == "4")
			{
				if (stateOfCalc == 3)
				{
					displayString = ac;
					stateOfCalc =0;
					signState = true;
				}
				else if(stateOfCalc == 1)
				{
					displayString = ac;
					stateOfCalc =2;		
				}
					else if(displayString == "0")	{
					signState = true;
					displayString = ac;
				}
				else
					displayString = displayString + ac;
				calcDisplay.setText(displayString);
			}
			else if(ac == "5")
			{
				if (stateOfCalc == 3)
				{
					displayString = ac;
					stateOfCalc =0;
					signState = true;
				}
				else if(stateOfCalc == 1)
				{
					displayString = ac;
					stateOfCalc =2;		
				}
					else if(displayString == "0")	{
					signState = true;
					displayString = ac;
				}
				else
					displayString = displayString + ac;
				calcDisplay.setText(displayString);	
			}
			else if(ac == "6")
			{
			if (stateOfCalc == 3)
				{
					displayString = ac;
					stateOfCalc =0;
					signState = true;
				}
				else if(stateOfCalc == 1)
				{
					displayString = ac;
					stateOfCalc =2;		
					signState = true;
				}
					else if(displayString == "0")	{
					signState = true;
					displayString = ac;
				}
				else
					displayString = displayString + ac;
				calcDisplay.setText(displayString);
			}
			else if(ac == "7")
			{
			if (stateOfCalc == 3)
				{
					displayString = ac;
					stateOfCalc =0;
					signState = true;
				}
				else if(stateOfCalc == 1)
				{
					displayString = ac;
					stateOfCalc =2;
					signState = true;
				}
					else if(displayString == "0")	{
					signState = true;
					displayString = ac;
				}
				else
					displayString = displayString + ac;
				calcDisplay.setText(displayString);
			}
			else if(ac == "8")
			{
			if (stateOfCalc == 3)
				{
					displayString = ac;
					signState = true;
					stateOfCalc =0;				
				}
				else if(stateOfCalc == 1)
				{
					displayString = ac;
					stateOfCalc =2;		
					signState = true;
				}
					else if(displayString == "0")	{
					signState = true;
					displayString = ac;
				}
				else
					displayString = displayString + ac;
				calcDisplay.setText(displayString);
			} 
			else if(ac == "9")
			{
			if (stateOfCalc == 3)
				{
					displayString = ac;
					stateOfCalc =0;	
					signState = true;				
				}
				else if(stateOfCalc == 1)
				{
					displayString = ac;
					stateOfCalc =2;	
					signState = true;
				}
				else if(displayString == "0")	{
					signState = true;
					displayString = ac;
				}
				else
					displayString = displayString + ac;
				calcDisplay.setText(displayString);
			}
			else if(ac == "0")
			{
			if (stateOfCalc == 3)
				{
					displayString = ac;
					signState = true;
					stateOfCalc =0;					
				}
				else if(stateOfCalc == 1)
				{
					displayString = ac;
					signState = true;
					stateOfCalc =2;		
				}
				else if(displayString == "0")
				{
					signState = true;
					displayString = ac;
				}
				else
					displayString = displayString + ac;
				
				calcDisplay.setText(displayString);
			}
			
		}
		
		if(ac == "=")
		{			
			//performs task according to state of calc
			if(stateOfCalc >= 1)
			{
				mem.writeMem(DATASENTREG, 1, DATA);
				if(stateOfCalc == 3)
					mem.writeMem(LEFTOPERANDREG, Formatter.stringToInt(displayString), DATA);	
				else if(stateOfCalc == 2)
					mem.writeMem(RIGHTOPERANDREG, Formatter.stringToInt(displayString), DATA);	
				else
					mem.writeMem(RIGHTOPERANDREG, mem.readMem(LEFTOPERANDREG, DATA), DATA);
				mem.writeMem(DATASENTREG, 1, DATA);
				setEnabledButtons(false);			
			}
						
		}
		//for each operator, writes left operand to LEFTOPERANDREG, writes operator code to 
		//OPERATORREG, and changes stateOfCalc to 1
		if(ac == "+")
		{			
		  stateOfCalc=1;			
			mem.writeMem(LEFTOPERANDREG, Formatter.stringToInt(displayString), DATA);	
			mem.writeMem(OPERATORREG, ADD, DATA);	
			signState = true;
		}
		if(ac == "-")
		{
			stateOfCalc=1;
			mem.writeMem(LEFTOPERANDREG, Formatter.stringToInt(displayString), DATA);						
			mem.writeMem(OPERATORREG, SUB, DATA);
			signState = true;
		}
		if(ac == "*")
		{
			stateOfCalc=1;
			mem.writeMem(LEFTOPERANDREG, Formatter.stringToInt(displayString), DATA);				
			mem.writeMem(OPERATORREG, MULT, DATA);	
		  signState = true;
		}
		if(ac == "/")
		{
			stateOfCalc=1;
			mem.writeMem(LEFTOPERANDREG, Formatter.stringToInt(displayString), DATA);			
			mem.writeMem(OPERATORREG, DIVIDE, DATA);
			signState = true;			
		}
		//"CE" - clears current entry into calc, resets display.
		if(ac == "CE")
		{
			displayString = "0";
			calcDisplay.setText(displayString);
		}
		//"C" - clears all nums and current operator in memory
		if(ac == "C")
		{
			displayString = "0";
			calcDisplay.setText(displayString);
			stateOfCalc = 0;
			resetNumReg();
		}
		if(ac == "On/Off")
		{
			if(powerState)
			{
				powerState = false;
				mem.writeMem(DONEREG, 0, DATA);	
				displayString = "0";
				calcDisplay.setText(displayString);
				setAllEnabled(false);
				stateOfCalc = 0;
				resetMem();
			}
			else
			{
				powerState = true;
				mem.writeMem(DONEREG, 1, DATA);										
				setAllEnabled(true);
			}
		}
		if(ac == "+/-")
		{			
			if(displayString != "0" && stateOfCalc !=1)
			{				
				if(signState)
				{
					displayString = "-" + displayString;
					signState = false;
					calcDisplay.setText(displayString);
				}
				else
				{
					displayString = displayString.substring(1);
					signState = true;
					calcDisplay.setText(displayString);
				}
				
			}
		}
	}//actionPerformed
	
	
public void contentsChanged(ListDataEvent e) 
	{
	//Sent when the contents of the list has changed in a way that's too complex to characterize with the previous methods.
		if(mem.readMem(DATASENTREG, DATA) == 0 && stateOfCalc > 1)
		{
			displayString = "" + (short)mem.readMem(ANSWERREG, DATA);
			if((short)mem.readMem(ANSWERREG, DATA) < 0)
				signState = false;
			else if((short)mem.readMem(ANSWERREG, DATA) > 0)
				signState = true;
			else				
			{
				signState = true;
				displayString = "0";
			}	
			calcDisplay.setText(displayString);	
			stateOfCalc = 3;
			setEnabledButtons(true);
			
		}
	}
public void intervalAdded(ListDataEvent e) 
	{
   // Sent after the indices in the index0,index1 interval have been inserted in the data model.
		
	}
public void intervalRemoved(ListDataEvent e) 
	{
		//Sent after the indices in the index0,index1 interval have been removed from the data model.
		
  }
	
	protected void resetNumReg()
	{
	//resets the regs containing left and right operands, the answer and the operator
		mem.writeMem(RIGHTOPERANDREG, 0, DATA);
		mem.writeMem(LEFTOPERANDREG, 0, DATA);
		mem.writeMem(OPERATORREG, 0, DATA);		
		mem.writeMem(ANSWERREG, 0, DATA);
	}
	
	protected void resetMem()
	{
		//resets all regsisters for calc
		mem.writeMem(RIGHTOPERANDREG, 0, DATA);
		mem.writeMem(LEFTOPERANDREG, 0, DATA);
		mem.writeMem(OPERATORREG, 0, DATA);
		mem.writeMem(DATASENTREG, 0, DATA);
		mem.writeMem(ANSWERREG, 0, DATA);
		mem.writeMem(DONEREG, 0, DATA);
	}
	
	private void turnOff()
	{//tells display there is no longer a calc running
		display.calcRunning = false;
	}	
	
	
	
	
}
