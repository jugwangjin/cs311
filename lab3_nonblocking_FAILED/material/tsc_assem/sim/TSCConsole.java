/*
© 2000, Performance Evaluation Laboratory, Brigham Young University. Reproduction of all or part of this work is permitted for non-profit educational
or research use provided this copyright notice remains intact. All other rights reserved. 
*/

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;


class TSCConsole extends JPanel implements ActionListener
{
  // Constants
	final static ImageIcon greenLightOnImage = Constants.greenLightOnImage;
  final static ImageIcon greenLightOffImage = Constants.greenLightOffImage;
  final static ImageIcon startButtonImage = 
      new ImageIcon("images/button-start.gif");
  final static ImageIcon stopButtonImage = 
	    new ImageIcon("images/button-stop.gif");
  final static ImageIcon stopPressedButtonImage = 
	    new ImageIcon("images/button-stop-pressed.gif");

	final static int REGDISPLAYWIDTH = 17; // Width of each text field
  final static String FONTNAME = Constants.FONTNAME;
  final static int FONTSTYLE = Constants.FONTSTYLE;
	final static int FONTSIZE = Constants.FONTSIZE;
	final static String DEFAULTREGDISPLAY = "0x0000";
	final static String MAXMEMSTRING = "0x0000: 0x0000 ";
	final static int DATA = Constants.DATA;
	final static int INSTRUCTION = Constants.INSTRUCTION;
	final static int CONSOLE_ACCESS = Constants.CONSOLE_ACCESS;
  final static int CURRENT_INST = Constants.CURRENT_INST;
  final static int CURRENT_DATA = Constants.CURRENT_DATA;
  final static int NO_HIGHLIGHT = Constants.NO_HIGHLIGHT;
  final static Insets ZERO_INSET = Constants.ZERO_INSET;

	private ImageIcon wallpaper;
	private int wallpaperWidth;
	private int wallpaperHeight;

  private Display display;
  private Memory mem;
	private Registers regs;
  private Monitor monitor;
	
  public JTextField pcDisplay;
  public JTextField gpr0Display;
  public JTextField gpr1Display;
  public JTextField gpr2Display;
  public JTextField gpr3Display;
  public JList memDisplay;
	private JButton loadPCButton;
	private JButton loadMemButton;
  public JButton startStopButton;
	public JButton clearButton;
	private JRadioButton lights[] = new JRadioButton[16];
  private GridBagLayout gridBag;
	private GridBagConstraints gbc;

  // Set when an address in mem should be highlighted.
	public int highlightMemStatus = NO_HIGHLIGHT;




  public TSCConsole(Display display)
	{
	  this.display = display;
		this.mem = display.mem;
		this.regs = display.regs;
		this.monitor = display.monitor;

		gridBag = new GridBagLayout();
	  gbc = new GridBagConstraints();
		setLayout(gridBag);
    setBackground(Constants.BORING_GRAY_2);
    setBorder(BorderFactory.createRaisedBevelBorder());
    add(createRegisterPanel());
		add(createMemoryPanel());
		createControls();
	}  // End of constructor




  // Called by the constructor
  private JPanel createRegisterPanel()
	{
		// Create register panel
    JPanel registerPanel = new JPanel(gridBag);
    registerPanel.setOpaque(false);
		gbc.weightx = 1;  gbc.weighty = 1;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.VERTICAL;
		gbc.insets = new Insets(0, 5, 0, 5);
    gridBag.setConstraints(registerPanel, gbc);

		// Create pc field
		JLabel pcLabel = new JLabel("PC", JLabel.CENTER);
		pcLabel.setForeground(Constants.BLACK);
		gbc.weightx = 0;  gbc.weighty = 0;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(15, 0, 1, 0);
    registerPanel.add(pcLabel, gbc);
		pcDisplay = new JTextField(DEFAULTREGDISPLAY, REGDISPLAYWIDTH);
		pcDisplay.setFont(Constants.FONT);
    pcDisplay.setBorder(BorderFactory.createLoweredBevelBorder());
		pcDisplay.setBackground(Constants.TEXT_BACKGROUND);
    pcDisplay.setEditable(false);
		pcDisplay.setToolTipText("Program Counter");
		gbc.insets = new Insets(1, 10, 0, 10);
    registerPanel.add(pcDisplay, gbc);

		// Create gpr0 field
		JLabel gpr0Label = new JLabel("GPR 0", JLabel.CENTER);
		gpr0Label.setForeground(Constants.BLACK);
		gbc.insets = new Insets(15, 0, 1, 0);
    registerPanel.add(gpr0Label, gbc);
		gpr0Display = new JTextField(DEFAULTREGDISPLAY, REGDISPLAYWIDTH);
		gpr0Display.setFont(Constants.FONT);
    gpr0Display.setBorder(BorderFactory.createLoweredBevelBorder());
		gpr0Display.setBackground(Constants.TEXT_BACKGROUND);
    gpr0Display.setEditable(false);
		gpr0Display.setToolTipText("General Purpose Register 0");
		gbc.insets = new Insets(1, 10, 0, 10);
    registerPanel.add(gpr0Display, gbc);

		// Create gpr1 field
		JLabel gpr1Label = new JLabel("GPR 1", JLabel.CENTER);
		gpr1Label.setForeground(Constants.BLACK);
		gbc.insets = new Insets(15, 0, 1, 0);
    registerPanel.add(gpr1Label, gbc);
		gpr1Display = new JTextField(DEFAULTREGDISPLAY, REGDISPLAYWIDTH);
		gpr1Display.setFont(Constants.FONT);
    gpr1Display.setBorder(BorderFactory.createLoweredBevelBorder());
		gpr1Display.setBackground(Constants.TEXT_BACKGROUND);
    gpr1Display.setEditable(false);
		gpr1Display.setToolTipText("General Purpose Register 1");
		gbc.insets = new Insets(1, 10, 0, 10);
    registerPanel.add(gpr1Display, gbc);

		// Create gpr2 field
		JLabel gpr2Label = new JLabel("GPR 2", JLabel.CENTER);
		gpr2Label.setForeground(Constants.BLACK);
		gbc.insets = new Insets(15, 0, 1, 0);
    registerPanel.add(gpr2Label, gbc);
		gpr2Display = new JTextField(DEFAULTREGDISPLAY, REGDISPLAYWIDTH);
		gpr2Display.setFont(Constants.FONT);
    gpr2Display.setBorder(BorderFactory.createLoweredBevelBorder());
		gpr2Display.setBackground(Constants.TEXT_BACKGROUND);
    gpr2Display.setEditable(false);
		gpr2Display.setToolTipText("General Purpose Register 2");
		gbc.insets = new Insets(1, 10, 0, 10);
    registerPanel.add(gpr2Display, gbc);

		// Create gpr3 field
		JLabel gpr3Label = new JLabel("GPR 3", JLabel.CENTER);
		gpr3Label.setForeground(Constants.BLACK);
		gbc.insets = new Insets(15, 0, 1, 0);
    registerPanel.add(gpr3Label, gbc);
		gpr3Display = new JTextField(DEFAULTREGDISPLAY, REGDISPLAYWIDTH);
		gpr3Display.setFont(Constants.FONT);
    gpr3Display.setBorder(BorderFactory.createLoweredBevelBorder());
		gpr3Display.setBackground(Constants.TEXT_BACKGROUND);
    gpr3Display.setEditable(false);
		gpr3Display.setToolTipText("General Purpose Register 3");
		gbc.insets = new Insets(1, 10, 0, 10);
    registerPanel.add(gpr3Display, gbc);

    return registerPanel;
  }  // End of createRegisterPanel




  // Called by the constructor
  private JPanel createMemoryPanel()
	{
	  JPanel memoryPanel = new JPanel(gridBag);
		memoryPanel.setOpaque(false);
		gbc.weightx = 1;  gbc.weighty = 1;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.fill = GridBagConstraints.BOTH;
 		gbc.insets = new Insets(10, 0, 0, 10);
    gridBag.setConstraints(memoryPanel, gbc);
		
    JLabel memLabel = new JLabel("Memory", JLabel.CENTER);
		memLabel.setForeground(Constants.BLACK);
		gbc.insets = new Insets(0, 0, 1, 0);
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0;  gbc.weighty = 0;
		memoryPanel.add(memLabel, gbc);

    memDisplay = new JList(mem);
		memDisplay.setBackground(Constants.TEXT_BACKGROUND);
    memDisplay.setFont(new Font(FONTNAME, FONTSTYLE, FONTSIZE-1));
    // Set the custom cell renderer
		memDisplay.setCellRenderer(new TextCellRenderer(
		    memDisplay.getFontMetrics(memDisplay.getFont())));
	  memDisplay.setPrototypeCellValue(MAXMEMSTRING);
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(1, 0, 0, 0);
		gbc.weightx = 1;  gbc.weighty = 1;
		memoryPanel.add(new JScrollPane(memDisplay), gbc);
		
		gbc.weightx = 0;  gbc.weighty = 0;   // Reset weights

		return memoryPanel;

	}  // End of createMemoryPanel




  /* This method doesn't create a panel. Components are simply 
	   added to the TSCConsole panel.  It should only be called
	   by the constructor	*/
	private void createControls()
	{
    // TSC Label
		JLabel tscLabel = new JLabel("TSC CONSOLE", JLabel.CENTER);
		tscLabel.setForeground(Constants.BLACK);
    Font tscFont = tscLabel.getFont();
		tscLabel.setFont(new Font(tscFont.getName(), tscFont.getStyle(), 20));
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0;  gbc.weighty = 0;
		gbc.insets = new Insets(12, 0, 8, 0);
		add(tscLabel, gbc);

    // Create "lights" panel
    JPanel lightsPanel = new JPanel(gridBag);
		lightsPanel.setOpaque(false);
		lightsPanel.setToolTipText("TSC Console Lights");
		gbc.insets = new Insets(0, 5, 0, 5);
    gridBag.setConstraints(lightsPanel, gbc);

    // Create the lights and add them to the lights panel
		for (int i = 0; i < 16; i++)  {
			lights[i] = new JRadioButton(greenLightOffImage);
	    lights[i].setSelectedIcon(greenLightOnImage);
	    lights[i].setHorizontalAlignment(AbstractButton.CENTER);
			lights[i].setMargin(new Insets(1, 1, 1, 1));
			lights[i].setOpaque(false);
		}
    gbc.gridwidth = 1;
		gbc.insets = new Insets(0, 10, 0, 0);
		lightsPanel.add(lights[15], gbc);
		gbc.insets = ZERO_INSET;
		lightsPanel.add(lights[14], gbc);
		lightsPanel.add(lights[13], gbc);
		lightsPanel.add(lights[12], gbc);
		gbc.insets = new Insets(0, 12, 0, 0);
		lightsPanel.add(lights[11], gbc);
		gbc.insets = ZERO_INSET;
		lightsPanel.add(lights[10], gbc);
		lightsPanel.add(lights[9], gbc);
		lightsPanel.add(lights[8], gbc);
		gbc.insets = new Insets(0, 12, 0, 0);
		lightsPanel.add(lights[7], gbc);
		gbc.insets = ZERO_INSET;
		lightsPanel.add(lights[6], gbc);
		lightsPanel.add(lights[5], gbc);
		lightsPanel.add(lights[4], gbc);
		gbc.insets = new Insets(0, 12, 0, 0);
		lightsPanel.add(lights[3], gbc);
		gbc.insets = ZERO_INSET;
		lightsPanel.add(lights[2], gbc);
		lightsPanel.add(lights[1], gbc);
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.insets = new Insets(0, 0, 0, 5);
		lightsPanel.add(lights[0], gbc);

		// Light labels (add to light panel)
		JLabel msbLabel = new JLabel("MSB", JLabel.LEFT);
		msbLabel.setForeground(Constants.BLACK);
		gbc.insets = new Insets(0, 5, 0, 0);
		gbc.gridwidth = 1;
		gbc.gridx=0;   gbc.gridy=1;
		lightsPanel.add(msbLabel, gbc);
		JLabel lsbLabel = new JLabel("LSB", JLabel.RIGHT);
		lsbLabel.setForeground(Constants.BLACK);
		gbc.insets = new Insets(0, 0, 0, 5);
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.gridx=15;
		lightsPanel.add(lsbLabel, gbc);
		add(lightsPanel);

		// Load buttons
		loadPCButton = new JButton(new ImageIcon("images/button-loadpc.gif"));
    loadPCButton.setPressedIcon(new ImageIcon("images/button-loadpc-pressed.gif"));
		loadPCButton.setOpaque(false);
    loadPCButton.setBorderPainted(false);
    loadPCButton.setContentAreaFilled(false);
		loadPCButton.setFocusPainted(false);
		loadPCButton.setMargin(ZERO_INSET);
		loadPCButton.setActionCommand("load pc");
		loadPCButton.addActionListener(this);
		loadPCButton.setToolTipText("Load the numeric value represented by the " +
		                                "Console Lights into the Program Counter");
		gbc.gridwidth = 1;
		gbc.gridx = GridBagConstraints.RELATIVE;
		gbc.gridy = GridBagConstraints.RELATIVE;
		gbc.insets = new Insets(10, 0, 10, 0);
    add(loadPCButton, gbc);		

		loadMemButton = new JButton(new ImageIcon("images/button-loadmem.gif"));
    loadMemButton.setPressedIcon(new ImageIcon("images/button-loadmem-pressed.gif"));
		loadMemButton.setOpaque(false);
    loadMemButton.setBorderPainted(false);
    loadMemButton.setContentAreaFilled(false);
		loadMemButton.setFocusPainted(false);
		loadMemButton.setMargin(ZERO_INSET);
		loadMemButton.setActionCommand("load mem");
		loadMemButton.addActionListener(this);
		loadMemButton.setToolTipText(
		  "Load the numeric value represented by the Console Lights into " +
			"the memory location pointed to by the number in the Program Counter");
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.insets = new Insets(10, 0, 10, 20);
    add(loadMemButton, gbc);

    // Start/Stop button
		startStopButton = new JButton(startButtonImage);
    startStopButton.setPressedIcon(stopPressedButtonImage);
		startStopButton.setOpaque(false);
    startStopButton.setBorderPainted(false);
    startStopButton.setContentAreaFilled(false);
		startStopButton.setFocusPainted(false);
		startStopButton.setMargin(ZERO_INSET);
		startStopButton.setActionCommand("start");
		startStopButton.addActionListener(this);
		startStopButton.setToolTipText("Start/Stop program execution");
		gbc.gridwidth = 1;
		gbc.insets = new Insets(0, 0, 10, 0);
    add(startStopButton, gbc);
		
    // Clear button
		clearButton = new JButton(new ImageIcon("images/button-clear.gif"));
    clearButton.setPressedIcon(new ImageIcon("images/button-clear-pressed.gif"));
		clearButton.setOpaque(false);
    clearButton.setBorderPainted(false);
    clearButton.setContentAreaFilled(false);
		clearButton.setFocusPainted(false);
		clearButton.setMargin(ZERO_INSET);
		clearButton.setActionCommand("clear");
		clearButton.addActionListener(this);
		clearButton.setToolTipText("Zero-out the registers");
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.insets = new Insets(0, 0, 10, 20);
    add(clearButton, gbc);
	}	  // End of createControls
  
	
	
	
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
		
		// Change the border color
		setBackground(color);
	}
	
	
	

	// Returns the value represented by the lights
	private int getLightsValue()
	{
	  int value = 0;
		
    for (int i = 0; i < 16; i++)
      if (lights[i].isSelected())
		    value = value | (1 << i);

    return value;
	}  // End of getLightsValue




  /* Sets the binary representation of 'value' 
	   into the lights. */
	public void setLights(int value)
	{
    for (int i = 0; i < 16; i++)  {
      if ((value & 0x00000001) == 1)
			  lights[i].setSelected(true);
			else
			  lights[i].setSelected(false);
	    value = value >> 1;
		}
	}  // End of setLights
	



  /* Disables (or enables) all buttons whose actions could 
	   improperly affect the state of the simulator when it is 
		 executing.  Called when 'start' or 'step' is pressed. */
  public void setEnabledButtons(boolean enable)
	{
		startStopButton.setEnabled(!display.debugConsole.step);
		loadPCButton.setEnabled(enable);
		loadMemButton.setEnabled(enable);
		clearButton.setEnabled(enable);
	} // End of setEnabledButtons




  /* Makes sure that 'location' is visible in the mem display 
	   window.  If 'top' is true, then the location will be 
	   visible at the top of the window.  */
  public void ensureMemLocationIsVisible(int location, boolean top)
	{
		if (top)  memDisplay.ensureIndexIsVisible(Memory.MEMSIZE-10);
    memDisplay.ensureIndexIsVisible(location);
  }  // End of ensureMemLocationIsVisible




  /* Highlights the appropriate address in the memory window
	   and makes sure the address is visible. Currently, only 
		 DATA and INSTRUCTION accesses are highlighted.  All other
		 access types clear the highlight. */
  public void highlightMem(int address, int accessType)
  {
		if (((accessType == INSTRUCTION) && (highlightMemStatus == CURRENT_INST))
	    || ((accessType == DATA) && (highlightMemStatus == CURRENT_DATA)))
	  {
		  memDisplay.setSelectedIndex(address);
      ensureMemLocationIsVisible(address, false);
		}
    else if (highlightMemStatus == NO_HIGHLIGHT)
  	  memDisplay.clearSelection();
	} // End of highlightMem




  // Responds to all button events in the TSC Console
  public void actionPerformed(ActionEvent e)
	{
	  String ac = e.getActionCommand();
		JButton source = (JButton) e.getSource();
		
    if (ac.equals("start"))  {
			// Notify the Sim that start has been pressed
      DebugConsole dc = display.debugConsole;
		  monitor.startPressed(false, dc.getStepSize(), dc.isBreakpointSet(),
                                      dc.getBreakpoint(), dc.isTraceOn());
			display.setEnabledConsoleButtons(false);   
		  source.setIcon(stopButtonImage);
			source.setActionCommand("stop");
		}
		
		else if (ac.equals("stop"))  {
		  IOConsole io = display.ioConsole;
      // stopPressed() must be called BEFORE cancelling I/O
		  monitor.stopPressed();
			if (io.inputConsoleCheckBox.isSelected())
        monitor.cancelInputData();  // If Sim is waiting for data, cancel it
			if (io.outputConsoleCheckBox.isSelected() 
			  && !io.autoAckCheckBox.isSelected())
          monitor.cancelOutputAck();  // If Sim is waiting for ack, cancel it
			display.setEnabledConsoleButtons(true);
		  source.setIcon(startButtonImage);
			source.setActionCommand("start");
		}
		
		else if (ac.equals("load pc"))  {
      regs.writeReg(Registers.PC, getLightsValue());
		}
		
		else if (ac.equals("load mem"))  {
		  int addr = regs.readReg(Registers.PC);
		  mem.writeMem(addr, getLightsValue(), CONSOLE_ACCESS);
			regs.writeReg(Registers.PC, addr + 1);  // Increment the PC

			// CONSOLE_ACCESS doesn't highlight memory so do it here
			highlightMem(addr, DATA);
		}
		
		else if (ac.equals("clear"))  {
		  if (!display.clearPromptItem.isSelected())  {
				if (new YesNoDialog(display).show(
				    "Clear Registers", "Clear Registers - Are you sure?")
						                                  == YesNoDialog.YES)  {
					regs.clearRegs();
				}
			}
			else   regs.clearRegs();
		}
  }  // End of actionPerformed

}  // End of TSCConsole class

