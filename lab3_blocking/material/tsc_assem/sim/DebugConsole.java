/*
© 2000, Performance Evaluation Laboratory, Brigham Young University. Reproduction of all or part of this work is permitted for non-profit educational
or research use provided this copyright notice remains intact. All other rights reserved. 
*/


import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;


class DebugConsole extends JPanel implements ActionListener,
                                             DocumentListener
{
  final static Insets ZERO_INSET = Constants.ZERO_INSET;
	final static int DISPLAYWIDTH = 7;  // Width of text windows
  final static int CURRENT_INST = Constants.CURRENT_INST;
  final static int CURRENT_DATA = Constants.CURRENT_DATA;
  final static int NO_HIGHLIGHT = Constants.NO_HIGHLIGHT;
	final static int DECIMAL = Constants.DECIMAL;
	final static int HEX = Constants.HEX;
	final static int NONE = Constants.NONE;
  final static ImageIcon redLightOnImage = Constants.redLightOnImage;
  final static ImageIcon redLightOffImage = Constants.redLightOffImage;
  final static ImageIcon stepButtonImage = 
	                                  new ImageIcon("images/button-step.gif");
  final static ImageIcon stopButtonImage = 
	                            new ImageIcon("images/button-stop-small.gif");
  final static ImageIcon stopPressedButtonImage = 
	                    new ImageIcon("images/button-stop-small-pressed.gif");

  private ImageIcon wallpaper;
	private int wallpaperWidth;
	private int wallpaperHeight;

  private Display display;
	private Monitor monitor;
	private InstructionTrace instTrace;
	private DbgKyLstnr debugKeyListener;
  private ButtonBugFix iconBugFix;
	
	public boolean step = false;    // Set to true when 'step' is pressed
                                  // Used by the monitor and others
		
  public JList traceDisplay;            // Used by the Instruction Trace
  public JButton stepButton;            // Used by the Monitor
	private JTextField stepSizeDisplay;
	private JTextField memLocationDisplay;
	private JButton viewMemButton;
  private JTextField breakpointDisplay;
	private JCheckBox enableBreakpointButton;
	private JCheckBox showPCButton;
	private JCheckBox showNumberButton;
	private JCheckBox disableTraceButton;
	public JButton clearTraceButton;       // Used by the Display
	private JCheckBox currentInstButton;
	private JCheckBox currentDataButton;
	private JCheckBox noneButton;
	private GridBagLayout gridBag;
	private GridBagConstraints gbc;




  // Constructor that makes the panel
	public DebugConsole(Display display)
	{
	  this.display = display;
		this.monitor = display.monitor;
	  this.instTrace = display.instTrace;
		
		// Create one key listener for all three text fields.
		this.debugKeyListener = new DbgKyLstnr();
		
		// Create one button bug fixer class for all buttons.
    iconBugFix = new ButtonBugFix();

    // Initialize various things
    setBackground(Constants.BORING_GRAY_2);
    setBorder(BorderFactory.createRaisedBevelBorder());
		gridBag = new GridBagLayout();  // gridbag used for all components
    setLayout(gridBag);
    gbc = new GridBagConstraints();  // gbc is used for all components
		
    // Create Trace Panel
    JPanel tracePanel = new JPanel(gridBag);
		tracePanel.setOpaque(false);
		gbc.weightx = 1;  gbc.weighty = 1;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(10, 10, 10, 10);
    gridBag.setConstraints(tracePanel, gbc);

    // Label for Trace Panel
    JLabel traceLabel = new JLabel("Instruction Trace", JLabel.CENTER);
		traceLabel.setForeground(Constants.BLACK);
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 1, 0);
		gbc.weightx = 0;  gbc.weighty = 0;
		tracePanel.add(traceLabel, gbc);

    // Create Trace Scroll Pane
    traceDisplay = new JList(instTrace);
		traceDisplay.setBackground(Constants.TEXT_BACKGROUND);
		traceDisplay.setFont(new Font(Constants.FONTNAME, Constants.FONTSTYLE,
		                                                Constants.FONTSIZE-1));
      // Set the custom cell renderer
		traceDisplay.setCellRenderer(new TextCellRenderer(
	      traceDisplay.getFontMetrics(traceDisplay.getFont())));
	  traceDisplay.setPrototypeCellValue("000: 0x0000: BEQ $0, $0, -128");
		traceDisplay.setFixedCellWidth(200);
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1;  gbc.weighty = 1;
		gbc.insets = new Insets(1, 0, 0, 0);
		tracePanel.add(new JScrollPane(traceDisplay, 
		    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), gbc);
    add(tracePanel);
		
		/* Now Create the rest of the Debug Console. It consists of
		   a top right and a bottom right panel where the bottom panel
			 is inside the top right panel.  */
    JPanel topRightPanel = new JPanel(gridBag);
		topRightPanel.setOpaque(false);
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.fill = GridBagConstraints.VERTICAL;
		gbc.weightx = 1;  gbc.weighty = 1;
		gbc.insets = ZERO_INSET;
    gridBag.setConstraints(topRightPanel, gbc);
		
    // Debug Label
		JLabel debugLabel = new JLabel("DEBUG CONSOLE", JLabel.CENTER);
		debugLabel.setForeground(Constants.BLACK);
    Font oldFont = debugLabel.getFont();
		debugLabel.setFont(new Font(oldFont.getName(), oldFont.getStyle(), 20));
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0;  gbc.weighty = 0;
		gbc.insets = new Insets(10, 0, 5, 0);
		topRightPanel.add(debugLabel, gbc);

    // Create Stepsize label and text field
    JLabel stepSizeLabel = new JLabel("Step Size", JLabel.RIGHT);
		stepSizeLabel.setForeground(Constants.BLACK);
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(15, 0, 0, 5);
		topRightPanel.add(stepSizeLabel, gbc);
		stepSizeDisplay = new JTextField("1", DISPLAYWIDTH);
		stepSizeDisplay.setFont(Constants.FONT);
    stepSizeDisplay.setBorder(BorderFactory.createLoweredBevelBorder());
		stepSizeDisplay.setBackground(Constants.TEXT_BACKGROUND);
		stepSizeDisplay.getDocument().addDocumentListener(this);
		stepSizeDisplay.addKeyListener(debugKeyListener);
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.insets = new Insets(15, 0, 0, 0);
    topRightPanel.add(stepSizeDisplay, gbc);
		
		// Create Step button
		stepButton = new JButton(stepButtonImage);
    stepButton.setPressedIcon(stopPressedButtonImage);
    stepButton.setBorderPainted(false);
    stepButton.setContentAreaFilled(false);
		stepButton.setFocusPainted(false);
		stepButton.setMargin(ZERO_INSET);
		stepButton.setActionCommand("step");
		stepButton.addActionListener(this);
		stepButton.setToolTipText(
		  "Exececute the number of instructions specified in the Step Size field");
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.insets = new Insets(15, 10, 0, 6);
    topRightPanel.add(stepButton, gbc);		

    // Create View Memory label and text field
    JLabel memLocationLabel = new JLabel("Mem Location", JLabel.LEFT);
		memLocationLabel.setForeground(Constants.BLACK);
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(15, 0, 0, 5);
		topRightPanel.add(memLocationLabel, gbc);
		memLocationDisplay = new JTextField("", DISPLAYWIDTH);
		memLocationDisplay.setFont(Constants.FONT);
    memLocationDisplay.setBorder(BorderFactory.createLoweredBevelBorder());
		memLocationDisplay.setBackground(Constants.TEXT_BACKGROUND);
		memLocationDisplay.setActionCommand("mem location");
		memLocationDisplay.addActionListener(this);
		memLocationDisplay.getDocument().addDocumentListener(this);
		memLocationDisplay.addKeyListener(debugKeyListener);
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.insets = new Insets(15, 0, 0, 0);
    topRightPanel.add(memLocationDisplay, gbc);

		// Create View Memory button
		viewMemButton = new JButton(new ImageIcon("images/button-view.gif"));
    viewMemButton.setPressedIcon(new ImageIcon("images/button-view-pressed.gif"));
    viewMemButton.setBorderPainted(false);
    viewMemButton.setContentAreaFilled(false);
		viewMemButton.setFocusPainted(false);
		viewMemButton.setMargin(ZERO_INSET);
		viewMemButton.setActionCommand("view memory");
		viewMemButton.addActionListener(this);
		viewMemButton.setToolTipText(
		  "Make the memory address specified in the Memory Location field " +
			"visable in the Memory window");
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.insets = new Insets(15, 10, 0, 6);
    topRightPanel.add(viewMemButton, gbc);		

    // Create Breakpoint label and text field
    JLabel breakpointLabel = new JLabel("Breakpoint", JLabel.RIGHT);
		breakpointLabel.setForeground(Constants.BLACK);
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(15, 0, 10, 5);
		topRightPanel.add(breakpointLabel, gbc);
		breakpointDisplay = new JTextField("", DISPLAYWIDTH);
		breakpointDisplay.setFont(Constants.FONT);
    breakpointDisplay.setBorder(BorderFactory.createLoweredBevelBorder());
		breakpointDisplay.setBackground(Constants.TEXT_BACKGROUND);
		breakpointDisplay.getDocument().addDocumentListener(this);
		breakpointDisplay.addKeyListener(debugKeyListener);
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.insets = new Insets(15, 0, 10, 0);
    topRightPanel.add(breakpointDisplay, gbc);

		// Create Enable Breakpoint check box
		enableBreakpointButton = new JCheckBox("Enable", redLightOffImage, false);
		enableBreakpointButton.setOpaque(false);
    enableBreakpointButton.setSelectedIcon(redLightOnImage);
    enableBreakpointButton.setDisabledIcon(redLightOffImage);
    enableBreakpointButton.addItemListener(iconBugFix);
		enableBreakpointButton.setFocusPainted(false);
		enableBreakpointButton.setToolTipText("Enable the breakpoint");
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.insets = new Insets(15, 10, 10, 6);
    topRightPanel.add(enableBreakpointButton, gbc);		

    // Create Bottom Right Panel. It consists of a highlight mem
		// panel and a trace controls panel.
    JPanel bottomRightPanel = new JPanel(gridBag);
    bottomRightPanel.setOpaque(false);
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.insets = ZERO_INSET;
    gridBag.setConstraints(bottomRightPanel, gbc);

		// Create Trace Control Panel (TCP) and label
    JPanel traceControlPanel = new JPanel(gridBag);
    traceControlPanel.setOpaque(false);
		gbc.gridwidth = 1;
		gbc.insets = new Insets(0, 5, 6, 0);
		bottomRightPanel.add(traceControlPanel, gbc);
		
    JLabel traceControlLabel = new JLabel("Trace", JLabel.CENTER);
		traceControlLabel.setForeground(Constants.BLACK);
    gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.insets = new Insets(5, 0, 5, 0);
		gbc.anchor = GridBagConstraints.CENTER;
		traceControlPanel.add(traceControlLabel, gbc);
		
		// Create Trace Control check boxes and add them to the TCP
		showNumberButton = new JCheckBox("Show Count", redLightOffImage);
    showNumberButton.setOpaque(false);
    showNumberButton.setSelectedIcon(redLightOnImage);
    showNumberButton.setDisabledIcon(redLightOffImage);
    showNumberButton.addItemListener(iconBugFix);
		showNumberButton.setFocusPainted(false);
		showNumberButton.setActionCommand("show number");
		showNumberButton.addActionListener(this);
		showNumberButton.setToolTipText(
		  "Show the instruction count in the trace (executing HLT " + 
			"resets the count)");
		gbc.insets = ZERO_INSET;
		gbc.anchor = GridBagConstraints.WEST;
    traceControlPanel.add(showNumberButton, gbc);

		showPCButton = new JCheckBox("Show PC", redLightOffImage, true);
    showPCButton.setOpaque(false);
    showPCButton.setSelectedIcon(redLightOnImage);
    showPCButton.setDisabledIcon(redLightOnImage);
    showPCButton.addItemListener(iconBugFix);
		showPCButton.setFocusPainted(false);
		showPCButton.setActionCommand("show pc");
		showPCButton.addActionListener(this);
		showPCButton.setToolTipText(
		  "Show the current PC next to each instruction in the trace");
		traceControlPanel.add(showPCButton, gbc);

		disableTraceButton = new JCheckBox("Disable Trace", redLightOffImage);
    disableTraceButton.setOpaque(false);
    disableTraceButton.setSelectedIcon(redLightOnImage);
    disableTraceButton.setDisabledIcon(redLightOffImage);
    disableTraceButton.addItemListener(iconBugFix);
		disableTraceButton.setFocusPainted(false);
		disableTraceButton.addActionListener(this);
		disableTraceButton.setToolTipText(
		  "Stop generating the instruction trace");
		traceControlPanel.add(disableTraceButton, gbc);

    // Create Clear Trace Button and add it to the TCP
		clearTraceButton = new JButton(new ImageIcon("images/button-cleartrace.gif"));
    clearTraceButton.setPressedIcon(
		                       new ImageIcon("images/button-cleartrace-pressed.gif"));
    clearTraceButton.setOpaque(false);
    clearTraceButton.setBorderPainted(false);
    clearTraceButton.setContentAreaFilled(false);
		clearTraceButton.setFocusPainted(false);
		clearTraceButton.setMargin(ZERO_INSET);
		clearTraceButton.setActionCommand("clear trace");
		clearTraceButton.addActionListener(this);
		clearTraceButton.setToolTipText(
		  "Erase the instruction trace window");
		gbc.insets = new Insets(0, 12, 0, 0);
		traceControlPanel.add(clearTraceButton, gbc);


		// Create Highlight Mem panel and labels
    JPanel highlightMemPanel = new JPanel(gridBag);
    highlightMemPanel.setOpaque(false);
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.insets = new Insets(0, 15, 10, 0);
		gbc.anchor = GridBagConstraints.NORTHWEST;
		bottomRightPanel.add(highlightMemPanel, gbc);
    
		JLabel highlightMemLabel = new JLabel("Highlight Mem", JLabel.CENTER);
		highlightMemLabel.setForeground(Constants.BLACK);
		gbc.insets = new Insets(5, 0, 5, 0);
		gbc.anchor = GridBagConstraints.CENTER;
		highlightMemPanel.add(highlightMemLabel, gbc);

		// Create Highlight radio buttons and add to the HM panel
		currentInstButton = new JCheckBox("Current Instr.", redLightOffImage);
    currentInstButton.setSelectedIcon(redLightOnImage);
    currentInstButton.setDisabledIcon(redLightOnImage);
    currentInstButton.setOpaque(false);
    currentInstButton.addItemListener(iconBugFix);
		currentInstButton.setFocusPainted(false);
		currentInstButton.setActionCommand("current inst");
		currentInstButton.addActionListener(this);
		currentInstButton.setToolTipText(
		  "Highlight the memory location containing the instruction " +
			"currently being executed");
		gbc.insets = ZERO_INSET;
		gbc.anchor = GridBagConstraints.WEST;
		highlightMemPanel.add(currentInstButton, gbc);

		currentDataButton = new JCheckBox("Current Data", redLightOffImage);
    currentDataButton.setSelectedIcon(redLightOnImage);
    currentDataButton.setDisabledIcon(redLightOffImage);
    currentDataButton.setOpaque(false);
    currentDataButton.addItemListener(iconBugFix);
		currentDataButton.setFocusPainted(false);
		currentDataButton.setActionCommand("current data");
		currentDataButton.addActionListener(this);
		currentDataButton.setToolTipText(
		  "Highlight the memory location containing the data " +
			"currently being read or modified");
		highlightMemPanel.add(currentDataButton, gbc);

		noneButton = new JCheckBox("None", redLightOffImage, true);
    noneButton.setSelectedIcon(redLightOnImage);
    noneButton.setDisabledIcon(redLightOffImage);
    noneButton.setOpaque(false);
    noneButton.addItemListener(iconBugFix);
		noneButton.setFocusPainted(false);
		noneButton.setActionCommand("no highlights");
		noneButton.addActionListener(this);
		noneButton.setToolTipText("Don't highlight anything");
		highlightMemPanel.add(noneButton, gbc);
		
		// Create Highlight button group
		ButtonGroup highlightGroup = new ButtonGroup();
		highlightGroup.add(currentInstButton);
		highlightGroup.add(currentDataButton);
		highlightGroup.add(noneButton);
		
		topRightPanel.add(bottomRightPanel);
    add(topRightPanel);
		
	}  // End of DebugConsole constructor




	// Override the paintComponent method to draw the background 
	// wallpaper.  If 'wallpaper' is null then paint using the 
	// background color.
	public void paintComponent(Graphics g)
	{
	  if (wallpaper != null)
		  for (int i = 0; i < getHeight(); i = i + wallpaperHeight)
		    for (int j = 0; j < getWidth(); j = j + wallpaperWidth)
		       wallpaper.paintIcon(this, g, j, i);
		else  super.paintComponent(g);
	}  // End of paintComponent




	// Sets the background wallpaper.  'wallpaper' can be null.
	public void setWallpaper(ImageIcon wallpaper, Color color)
	{
		this.wallpaper = wallpaper;
		if (wallpaper != null)  {
		  this.wallpaperWidth = wallpaper.getIconWidth();
		  this.wallpaperHeight = wallpaper.getIconHeight();
	  }
		
		// Change the border colors
		setBackground(color);
	}
	
	
	

	 /* Disables (or enables) all buttons whose actions could 
	   improperly affect the state of the simulator when it is 
		 executing.  Called when 'start' or 'step' is pressed. */
  public void setEnabledButtons(boolean enable)
	{
    if (!step)
			stepButton.setEnabled(enable);
    breakpointDisplay.setEditable(enable);
    stepSizeDisplay.setEditable(enable);
	  enableBreakpointButton.setEnabled(enable);
    viewMemButton.setEnabled(enable);
    memLocationDisplay.setEditable(enable);
	  showPCButton.setEnabled(enable);
	  showNumberButton.setEnabled(enable);
	  disableTraceButton.setEnabled(enable);
	  clearTraceButton.setEnabled(enable);
	} // End of setEnabledButtons




  /* Clear all three text fields, sets step size to 1,
	   and disables the breakpoint. */
	public void clearTextFields()
	{
	  stepSizeDisplay.setText("1");
	  memLocationDisplay.setText("");
	  breakpointDisplay.setText("");
    enableBreakpointButton.setSelected(false);
	}  // End of clearTextFields()




  /* Returns the value of the step size field as an int. */
	public int getStepSize()
	{
		String text = stepSizeDisplay.getText();
	  
		if (text.length() > 0) {
		  int stepSize = Formatter.stringToShortInt(text);
  	  if (stepSize != 0)
			  return stepSize;
		}
	  
		// If this point is reached, the field is empty or is zero
		stepSizeDisplay.setText("1");  // 1 is the default step size
	  return 1;
	
	}  // End of getStepSize




  /* Returns the value of the breakpoint field. */
	public int getBreakpoint()
	{
	  String text = breakpointDisplay.getText();
		
		if (text.length() != 0)
  		return Formatter.stringToShortInt(text);
		else 
			return -1;
	}  // End of getBreakpoint




  /* Returns true if the breakpoint field has a number in it
	   and is not disabled.  Returns false if it is empty.  */
	public boolean isBreakpointSet()
	{
		if ((breakpointDisplay.getText().length() != 0) 
		  && (enableBreakpointButton.isSelected()))
  	  	return true;
		else
			return false;
	}  // End of isBreakpointSet




  /* Returns true if the instruction trace is enabled.  
	   Otherwise it returns false.  */
	public boolean isTraceOn()
	{
    if (disableTraceButton.isSelected())
 	  	return false;
		else
			return true;
	}  // End of isBreakpointSet




  /* Sets 'pcFormat' in the instruction trace.  If the Show PC box is
	   not checked, then the format is NONE.  If it is checked, then
		 the format is 'newFormat'.  */
	public void setPCFormat(int newFormat)
	{
	  if (showPCButton.isSelected())
		  instTrace.pcFormat = newFormat;
		else
		  instTrace.pcFormat = NONE;
	}  // End of setPCFormat

	
	

  /* The next four methods are used to check for any deletion or 
	   insertion of text.  The text fields are checked to see if the 
		 leading '0x' was backspaced over or otherwise messed up thus 
		 turning a hex number into garbage.  For example, deleting the 
		 x in '0xabcd' would create '0abcd' which is an invalid number. 
		 Invalid numbers are handled by simply deleting the entire line. */
  public void removeUpdate(DocumentEvent e)
  {
     checkNumber(e);  // Checks for deletions
	}  // End of removeUdate




	public void insertUpdate(DocumentEvent e)
  {
     checkNumber(e);  // Checks for insertions
	}  // End of insertUpdate




  /* This method is only needed to fulfill DebugConsole's role 
	   as a DocumentListener. */
	public void changedUpdate(DocumentEvent e)  {}




  // Is the number a valid number?
  private void checkNumber(DocumentEvent e)
	{
		PlainDocument source = (PlainDocument) e.getDocument();

		if (source.getLength() != 0) {
  		try {
		    String text = source.getText(0, source.getLength());
      	if (!Formatter.isStringValidHexOrDec(text))  {
            /* The number is garbage so erase it (for lack of 
               a better way to fix it).  */
				    /* There is a write lock on the document so insert a text
					     removal method into the event thread to erase the text
  					   when the document is unlocked.  */
		  	  SwingUtilities.invokeLater(new RmvText(source));
          Toolkit.getDefaultToolkit().beep();
  		  }
  		} catch (BadLocationException ble) {
		      System.out.println("BadLocationException in DebugConsole.insertUpdate(): " 
					                                                        + ble.getMessage());
			  }
		}
	}  // End of checkNumber




	/* This nested class is inserted into the event thread by 
	   checkNumber() to erase the text in a text field. */
  private class RmvText implements Runnable
	{
    PlainDocument doc;

		public RmvText(PlainDocument doc)
		{
		  this.doc = doc;
		}
		
		public void run()
		{
			try {
  			if (doc.getLength() != 0)
  				doc.remove(0, doc.getLength());
		  } 
			catch (BadLocationException ble)  {
		      System.out.println("BadLocationException in DebugConsole.run(): " 
					                                            + ble.getMessage());
			}
		}
  }  // End of RmvText
	



  /* DbgKyLstnr is a nested class that ensures only numbers and 
     some select letters can be entered into the text fields.  It does 
		 this by intercepting key presses before they are sent to the text
		 display.  It is called whenever a key event is generated. It was 
		 made a nested class so that it could extend KeyAdapter.  */
	private class DbgKyLstnr extends KeyAdapter 
	{
	  public void keyTyped(KeyEvent e)
		{
		  char c = e.getKeyChar();
			JTextField source = (JTextField) e.getSource();
			
      if (!Formatter.isCharValid(source.getText(), c))
				e.setKeyChar((char) 0);
			else
			  e.setKeyChar(Character.toLowerCase(c));
		}
	}  // End of DbgKyLstnr




  /* Responds to all button events in the debug Console 
	   and to a return in the mem location field. */
  public void actionPerformed(ActionEvent e)
	{
	  String ac = e.getActionCommand();
		
    if (ac.equals("step"))  {
		  step = true;
			// Notify the Sim that step has been pressed
		  monitor.startPressed(true, getStepSize(), isBreakpointSet(),
                                     getBreakpoint(), isTraceOn());
			display.setEnabledConsoleButtons(false);
      stepButton.setIcon(stopButtonImage);
			stepButton.setActionCommand("stop");
		}

		else if (ac.equals("stop"))  {
		  IOConsole io = display.ioConsole;
		  step = false;

      // stopPressed() must be called BEFORE cancelling I/O
		  monitor.stopPressed();
			if (io.inputConsoleCheckBox.isSelected())
        monitor.cancelInputData();  // If Sim is waiting for data, cancel it
			if (io.outputConsoleCheckBox.isSelected() 
			  && !io.autoAckCheckBox.isSelected())
          monitor.cancelOutputAck();  // If Sim is waiting for ack, cancel it
			display.setEnabledConsoleButtons(true);
      stepButton.setIcon(stepButtonImage);
			stepButton.setActionCommand("step");
		}

		else if (ac.equals("view memory"))  {
  		String number = memLocationDisplay.getText();
			if (!number.equals(""))
			  display.tscConsole.ensureMemLocationIsVisible(
			       Formatter.stringToShortInt(number), true);
		}

		else if (ac.equals("mem location"))  {
		  // Return was pressed in the text field.  Treat it as a button click.
			viewMemButton.doClick();
		}

		else if (ac.equals("current inst"))  {
		  display.tscConsole.highlightMemStatus = CURRENT_INST;
		}

		else if (ac.equals("current data"))  {
		  display.tscConsole.highlightMemStatus = CURRENT_DATA;
		}

		else if (ac.equals("no highlights"))  {
		  display.tscConsole.highlightMemStatus = NO_HIGHLIGHT;
		}

		else if (ac.equals("show pc"))  {
		  // 'pcFormat' might need to be set to NONE so call:
      setPCFormat(display.mem.memAddressFormat);
		}

		else if (ac.equals("show number"))  {
		  if (showNumberButton.isSelected())
			  instTrace.instNumberFormat = DECIMAL;
			else 
			  instTrace.instNumberFormat = NONE;
		}

		else if (ac.equals("clear trace"))  {
		  if (!display.clearPromptItem.isSelected())  {
				if (new YesNoDialog(display).show(
				    "Clear Trace", "Clear Trace - Are you sure?")
				                              == YesNoDialog.YES)  {
          instTrace.removeAllElements();
				}
			}
			else
        instTrace.removeAllElements();
		}

  }   // End of actionPerformed
		
}  // End of DebugConsole Class

