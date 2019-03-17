/*
© 2000, Performance Evaluation Laboratory, Brigham Young University. Reproduction of all or part of this work is permitted for non-profit educational
or research use provided this copyright notice remains intact. All other rights reserved. 
*/

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.swing.*;
import javax.swing.event.*;

/* This class assembles the components needed to create the TSC GUI.
   It creates the instances of the 3 consoles and contains the code 
	 for the menu bar.
	
	 The registers, the memory, the monitor, and the instruction trace 
	 all have pointers to this display so they can access any of the 
	 consoles and their public methods (using the event thread, of 
	 course).  The Sim must go through the monitor to get to the 
	 display and vice versa.  This insures that all Sim accesses to 
	 the GUI will be synchronized using the event thread and all GUI 
	 signals for the Sim will be synchronized.
*/

class Display extends JFrame implements ActionListener
{
	final static int HEX = Constants.HEX;
	final static int DECIMAL = Constants.DECIMAL;
	final static int SIGNED_DECIMAL = Constants.SIGNED_DECIMAL;
	final static int BINARY = Constants.BINARY;
	final static int PC = Constants.PC;
	final static int GPR0 = Constants.GPR0;
	final static int GPR1 = Constants.GPR1;
	final static int GPR2 = Constants.GPR2;
	final static int GPR3 = Constants.GPR3;
	
  public TSCConsole tscConsole; 
	public DebugConsole debugConsole;
  public IOConsole ioConsole;
	public Calc calc;
	
	public Memory mem;
	public Registers regs;
  public Monitor monitor;
	public InstructionTrace instTrace;
	private FileTools fileTools;
	private BckgrdChngr backgroundChanger;

	//flag - true when calculator is running, false otherwise
	public boolean calcRunning;

	public JCheckBoxMenuItem clearPromptItem;
  private JMenu fileMenu;
	private JMenu formatMenu;
	private JMenu clearMenu;
	private JMenu ioDeviceMenu;
	private JLabel inputFileLabel;     // The input file name
	private JLabel outputFileLabel;    // The output file name
	private JMenuBar mb;
  private JMenuItem resetInputFileItem;
  private JMenuItem closeInputFileItem;
  private JMenuItem resetOutputFileItem;
  private JMenuItem closeOutputFileItem;
  private JMenuItem clearMemItem;
	private JMenuItem gpr0HexItem;
	private JMenuItem gpr1HexItem;
	private JMenuItem gpr2HexItem;
	private JMenuItem gpr3HexItem;
	private JMenuItem gpr0DecItem;
	private JMenuItem gpr1DecItem;
	private JMenuItem gpr2DecItem;
	private JMenuItem gpr3DecItem;
	private JMenuItem gpr0SignedDecItem;
	private JMenuItem gpr1SignedDecItem;
	private JMenuItem gpr2SignedDecItem;
	private JMenuItem gpr3SignedDecItem;
	private JMenuItem gpr0BinaryItem;
	private JMenuItem gpr1BinaryItem;
	private JMenuItem gpr2BinaryItem;
	private JMenuItem gpr3BinaryItem;
	private JCheckBoxMenuItem metalItem;
	
	private GridBagLayout gridBag = new GridBagLayout();
	private GridBagConstraints gbc = new GridBagConstraints();
	



  public Display(Registers regs, Memory mem, Monitor monitor, 
                  	               InstructionTrace instTrace)
  {
    System.out.println("Initializing the display...");
    // Initialize simulator parts
    this.regs = regs;
    this.regs.setDisplay(this);
		this.mem = mem;
    this.mem.setDisplay(this);
		this.monitor = monitor;
    this.monitor.setDisplay(this);
		this.instTrace = instTrace;
		this.instTrace.setDisplay(this);
		
		// Instantiate the file tools and background changer
		fileTools = new FileTools(null);
		backgroundChanger = new BckgrdChngr();
		
		//initialize flag
		calcRunning = false;
		
    // Turn the tool tips off at startup
    ToolTipManager.sharedInstance().setEnabled(false);
		// Set the tips initial and dismiss delay
    ToolTipManager.sharedInstance().setInitialDelay(250);
    ToolTipManager.sharedInstance().setDismissDelay(8000);

    // Initialize the frame.
    getContentPane().setLayout(gridBag);
		
 
    // Add TSC Console to left half of frame
		tscConsole = new TSCConsole(this);
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.VERTICAL;   // Resize vertically
		gbc.weightx = 0;  gbc.weighty = 1;
		gbc.insets = new Insets(10, 10, 10, 10);
    getContentPane().add(tscConsole, gbc);

    // Create right half panel
		JPanel rightPanel = new JPanel(gridBag);
		rightPanel.setOpaque(false);
  	gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1;  gbc.weighty = 1;
		gbc.insets = new Insets(10, 0, 10, 10);
    gridBag.setConstraints(rightPanel, gbc);

		// Add Debug Console to the right panel
		debugConsole = new DebugConsole(this);
		gbc.insets = new Insets(0, 0, 10, 0);
		rightPanel.add(debugConsole, gbc);

		// Add I/O Console to the right panel
		ioConsole = new IOConsole(this);
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 0;  gbc.weighty = 0;
		gbc.insets = Constants.ZERO_INSET;
		rightPanel.add(ioConsole, gbc);

    // Add right panel to the frame
		getContentPane().add(rightPanel);
		
    // Add menu bar to the frame
		setJMenuBar(createMenuBar());

    // Set the wallpaper
		metalItem.doClick();
		
		// Respond to WindowClosing event
		addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
			  ioConsole.closeInputFile();
			  ioConsole.closeOutputFile();
        System.exit(0);
      }
	  });
	
	}  // End of constructor
  



  private JMenuBar createMenuBar()
	{
    mb = new JMenuBar();
		
		// Make the menu bar the same color as the frame.
    mb.setBackground(getContentPane().getBackground());

    // TSC Icon
		JMenu tscMenu = new JMenu();
    tscMenu.setOpaque(false);
    tscMenu.setIcon(new ImageIcon("images/TSCLogoSmall.gif"));
		mb.add(tscMenu);
    mb.add(new MenuPadding());
		
		JMenuItem creditItem = new JMenuItem("About");
		creditItem.setActionCommand("credits");
		creditItem.addActionListener(this);
		creditItem.setToolTipText("About the TSC Simulator");
		tscMenu.add(creditItem);


		// File menu
		fileMenu = new JMenu("File");  
    fileMenu.setOpaque(false);
		mb.add(fileMenu);
    mb.add(new MenuPadding());

		JMenuItem loadRomItem = new JMenuItem("Load From A ROM");
		loadRomItem.setToolTipText(
		     "Replace memory contents with the contents of a ROM file");
		loadRomItem.setActionCommand("load rom");
		loadRomItem.addActionListener(this);
		fileMenu.add(loadRomItem);

		JMenuItem makeRomItem = new JMenuItem("Burn To A ROM");
		makeRomItem.setToolTipText("Save the contents of memory in a ROM file");
	  makeRomItem.setActionCommand("burn rom");
	  makeRomItem.addActionListener(this);
	  fileMenu.add(makeRomItem);

		fileMenu.addSeparator();

		JMenuItem ioInputFileItem = new JMenuItem("Select Input File");
  	ioInputFileItem.setToolTipText(
		    "Select the file that the I/O Console will read from");
		ioInputFileItem.setActionCommand("select input file");
		ioInputFileItem.addActionListener(this);
		fileMenu.add(ioInputFileItem);

		resetInputFileItem = new JMenuItem("Reset Input File");
  	resetInputFileItem.setToolTipText(
		  "Set the input file pointer to point to the first word in the file");
		resetInputFileItem.setActionCommand("reset input file");
    resetInputFileItem.setEnabled(false);
		resetInputFileItem.addActionListener(this);
		fileMenu.add(resetInputFileItem);

		closeInputFileItem = new JMenuItem("Close Input File");
  	closeInputFileItem.setToolTipText("Close the input file");
		closeInputFileItem.setActionCommand("close input file");
    closeInputFileItem.setEnabled(false);
		closeInputFileItem.addActionListener(this);
		fileMenu.add(closeInputFileItem);

		inputFileLabel = new JLabel("      Input File:  none     ");
  	inputFileLabel.setToolTipText("The current input file");
		fileMenu.add(inputFileLabel);

		fileMenu.addSeparator();
		
		JMenuItem ioOutputFileItem = new JMenuItem("Select Output File");
  	ioOutputFileItem.setToolTipText(
		    "Select the file that the I/O Console will write to");
		ioOutputFileItem.setActionCommand("select output file");
		ioOutputFileItem.addActionListener(this);
		fileMenu.add(ioOutputFileItem);

		resetOutputFileItem = new JMenuItem("Reset Output File");
  	resetOutputFileItem.setToolTipText(
		  "Set the output file pointer to point to the beginning of the file");
		resetOutputFileItem.setActionCommand("reset output file");
    resetOutputFileItem.setEnabled(false);
		resetOutputFileItem.addActionListener(this);
		fileMenu.add(resetOutputFileItem);

		closeOutputFileItem = new JMenuItem("Close Output File");
  	closeOutputFileItem.setToolTipText("Close the output file");
		closeOutputFileItem.setActionCommand("close output file");
    closeOutputFileItem.setEnabled(false);
		closeOutputFileItem.addActionListener(this);
		fileMenu.add(closeOutputFileItem);

		outputFileLabel = new JLabel("      Output File:  none     ");
  	outputFileLabel.setToolTipText("The current input file");
		fileMenu.add(outputFileLabel);

		fileMenu.addSeparator();

		JMenuItem exitItem = new JMenuItem("Exit");
  	exitItem.setToolTipText("Exit the TSC Simulator");
		exitItem.setActionCommand("exit");
		exitItem.addActionListener(this);
		fileMenu.add(exitItem);


		// Format Menu
		formatMenu = new JMenu("Format");
    formatMenu.setOpaque(false);
		mb.add(formatMenu);
    mb.add(new MenuPadding());

    // Create PC menu and put it in format menu
		JMenu pcMenu = new JMenu("PC");
  	pcMenu.setToolTipText(
		    "Change the numeric representation of the PC register contents");
    formatMenu.add(pcMenu);

		JMenuItem pcHexItem = new JCheckBoxMenuItem("Hexadecimal", true);
		pcHexItem.setActionCommand("pc-hex");
		pcHexItem.addActionListener(this);
		pcMenu.add(pcHexItem);
		JMenuItem pcDecItem = new JCheckBoxMenuItem("Unsigned Decimal");
		pcDecItem.setActionCommand("pc-dec");
		pcDecItem.addActionListener(this);
		pcMenu.add(pcDecItem);
		JMenuItem pcBinaryItem = new JCheckBoxMenuItem("Binary");
		pcBinaryItem.setActionCommand("pc-binary");
		pcBinaryItem.addActionListener(this);
		pcMenu.add(pcBinaryItem);

    // PC register button group
		ButtonGroup pcRegisterGroup = new ButtonGroup();
		pcRegisterGroup.add(pcHexItem);
		pcRegisterGroup.add(pcDecItem);
		pcRegisterGroup.add(pcBinaryItem);
		
    // Create GPR 0 menu and put it in format menu
		JMenu gpr0Menu = new JMenu("GPR 0");
  	gpr0Menu.setToolTipText(
		    "Change the numeric representation of the GPR 0 contents");
    formatMenu.add(gpr0Menu);

		gpr0HexItem = new JCheckBoxMenuItem("Hexadecimal", true);
		gpr0HexItem.setActionCommand("gpr0-hex");
		gpr0HexItem.addActionListener(this);
		gpr0Menu.add(gpr0HexItem);
		gpr0DecItem = new JCheckBoxMenuItem("Unsigned Decimal");
		gpr0DecItem.setActionCommand("gpr0-dec");
		gpr0DecItem.addActionListener(this);
		gpr0Menu.add(gpr0DecItem);
		gpr0SignedDecItem = new JCheckBoxMenuItem("Signed Decimal");
		gpr0SignedDecItem.setActionCommand("gpr0-signed-dec");
		gpr0SignedDecItem.addActionListener(this);
		gpr0Menu.add(gpr0SignedDecItem);
		gpr0BinaryItem = new JCheckBoxMenuItem("Binary");
		gpr0BinaryItem.setActionCommand("gpr0-binary");
		gpr0BinaryItem.addActionListener(this);
		gpr0Menu.add(gpr0BinaryItem);

    // GPR 0 button group
		ButtonGroup gpr0RegisterGroup = new ButtonGroup();
		gpr0RegisterGroup.add(gpr0HexItem);
		gpr0RegisterGroup.add(gpr0DecItem);
		gpr0RegisterGroup.add(gpr0SignedDecItem);
		gpr0RegisterGroup.add(gpr0BinaryItem);
		
    // Create GPR 1 menu and put it in format menu
		JMenu gpr1Menu = new JMenu("GPR 1");
  	gpr1Menu.setToolTipText(
		    "Change the numeric representation of the GPR 1 contents");
    formatMenu.add(gpr1Menu);

		gpr1HexItem = new JCheckBoxMenuItem("Hexadecimal", true);
		gpr1HexItem.setActionCommand("gpr1-hex");
		gpr1HexItem.addActionListener(this);
		gpr1Menu.add(gpr1HexItem);
		gpr1DecItem = new JCheckBoxMenuItem("Unsigned Decimal");
		gpr1DecItem.setActionCommand("gpr1-dec");
		gpr1DecItem.addActionListener(this);
		gpr1Menu.add(gpr1DecItem);
		gpr1SignedDecItem = new JCheckBoxMenuItem("Signed Decimal");
		gpr1SignedDecItem.setActionCommand("gpr1-signed-dec");
		gpr1SignedDecItem.addActionListener(this);
		gpr1Menu.add(gpr1SignedDecItem);
		gpr1BinaryItem = new JCheckBoxMenuItem("Binary");
		gpr1BinaryItem.setActionCommand("gpr1-binary");
		gpr1BinaryItem.addActionListener(this);
		gpr1Menu.add(gpr1BinaryItem);

    // GPR 1 button group
		ButtonGroup gpr1RegisterGroup = new ButtonGroup();
		gpr1RegisterGroup.add(gpr1HexItem);
		gpr1RegisterGroup.add(gpr1DecItem);
		gpr1RegisterGroup.add(gpr1SignedDecItem);
		gpr1RegisterGroup.add(gpr1BinaryItem);
		
    // Create GPR 2 menu and put it in format menu
		JMenu gpr2Menu = new JMenu("GPR 2");
  	gpr2Menu.setToolTipText(
		    "Change the numeric representation of the GPR 2 contents");
    formatMenu.add(gpr2Menu);

		gpr2HexItem = new JCheckBoxMenuItem("Hexadecimal", true);
		gpr2HexItem.setActionCommand("gpr2-hex");
		gpr2HexItem.addActionListener(this);
		gpr2Menu.add(gpr2HexItem);
		gpr2DecItem = new JCheckBoxMenuItem("Unsigned Decimal");
		gpr2DecItem.setActionCommand("gpr2-dec");
		gpr2DecItem.addActionListener(this);
		gpr2Menu.add(gpr2DecItem);
		gpr2SignedDecItem = new JCheckBoxMenuItem("Signed Decimal");
		gpr2SignedDecItem.setActionCommand("gpr2-signed-dec");
		gpr2SignedDecItem.addActionListener(this);
		gpr2Menu.add(gpr2SignedDecItem);
		gpr2BinaryItem = new JCheckBoxMenuItem("Binary");
		gpr2BinaryItem.setActionCommand("gpr2-binary");
		gpr2BinaryItem.addActionListener(this);
		gpr2Menu.add(gpr2BinaryItem);

    // GPR 2 button group
		ButtonGroup gpr2RegisterGroup = new ButtonGroup();
		gpr2RegisterGroup.add(gpr2HexItem);
		gpr2RegisterGroup.add(gpr2DecItem);
		gpr2RegisterGroup.add(gpr2SignedDecItem);
		gpr2RegisterGroup.add(gpr2BinaryItem);
		
    // Create GPR 3 menu and put it in format menu
		JMenu gpr3Menu = new JMenu("GPR 3");
  	gpr3Menu.setToolTipText(
		    "Change the numeric representation of the GPR 3 contents");
    formatMenu.add(gpr3Menu);

		gpr3HexItem = new JCheckBoxMenuItem("Hexadecimal", true);
		gpr3HexItem.setActionCommand("gpr3-hex");
		gpr3HexItem.addActionListener(this);
		gpr3Menu.add(gpr3HexItem);
		gpr3DecItem = new JCheckBoxMenuItem("Unsigned Decimal");
		gpr3DecItem.setActionCommand("gpr3-dec");
		gpr3DecItem.addActionListener(this);
		gpr3Menu.add(gpr3DecItem);
		gpr3SignedDecItem = new JCheckBoxMenuItem("Signed Decimal");
		gpr3SignedDecItem.setActionCommand("gpr3-signed-dec");
		gpr3SignedDecItem.addActionListener(this);
		gpr3Menu.add(gpr3SignedDecItem);
		gpr3BinaryItem = new JCheckBoxMenuItem("Binary");
		gpr3BinaryItem.setActionCommand("gpr3-binary");
		gpr3BinaryItem.addActionListener(this);
		gpr3Menu.add(gpr3BinaryItem);

    // GPR 3 button group
		ButtonGroup gpr3RegisterGroup = new ButtonGroup();
		gpr3RegisterGroup.add(gpr3HexItem);
		gpr3RegisterGroup.add(gpr3DecItem);
		gpr3RegisterGroup.add(gpr3SignedDecItem);
		gpr3RegisterGroup.add(gpr3BinaryItem);

    // Create All GPRs menu and put it in format menu
		JMenu allGPRMenu = new JMenu("All Four GPRs");
  	allGPRMenu.setToolTipText(
		    "Change the numeric representation of all the GPR's contents");
    formatMenu.add(allGPRMenu);

		JMenuItem allGPRHexItem = new JCheckBoxMenuItem("Hexadecimal", true);
		allGPRHexItem.setActionCommand("allGPR-hex");
		allGPRHexItem.addActionListener(this);
		allGPRMenu.add(allGPRHexItem);
		JMenuItem allGPRDecItem = new JCheckBoxMenuItem("Unsigned Decimal");
		allGPRDecItem.setActionCommand("allGPR-dec");
		allGPRDecItem.addActionListener(this);
		allGPRMenu.add(allGPRDecItem);
		JMenuItem allGPRSignedDecItem = new JCheckBoxMenuItem("Signed Decimal");
		allGPRSignedDecItem.setActionCommand("allGPR-signed-dec");
		allGPRSignedDecItem.addActionListener(this);
		allGPRMenu.add(allGPRSignedDecItem);
		JMenuItem allGPRBinaryItem = new JCheckBoxMenuItem("Binary");
		allGPRBinaryItem.setActionCommand("allGPR-binary");
		allGPRBinaryItem.addActionListener(this);
		allGPRMenu.add(allGPRBinaryItem);

    // All GPRs button group
		ButtonGroup allGPRRegisterGroup = new ButtonGroup();
		allGPRRegisterGroup.add(allGPRHexItem);
		allGPRRegisterGroup.add(allGPRDecItem);
		allGPRRegisterGroup.add(allGPRSignedDecItem);
		allGPRRegisterGroup.add(allGPRBinaryItem);

    // Create Memory Addresses menu and put it in format menu
		JMenu memAddressMenu = new JMenu("Memory Addresses");
  	memAddressMenu.setToolTipText(
		    "Change the numeric representation of the memory addresses");

    JMenuItem memAddressHexItem = new JCheckBoxMenuItem("Hexadecimal", true);
		memAddressHexItem.setActionCommand("mem address-hex");
		memAddressHexItem.addActionListener(this);
		memAddressMenu.add(memAddressHexItem);
		JMenuItem memAddressDecItem = new JCheckBoxMenuItem("Unsigned Decimal");
		memAddressDecItem.setActionCommand("mem address-dec");
		memAddressDecItem.addActionListener(this);
		memAddressMenu.add(memAddressDecItem);
    formatMenu.add(memAddressMenu);

    // Memory Addresses button group
		ButtonGroup memAddressGroup = new ButtonGroup();
		memAddressGroup.add(memAddressHexItem);
		memAddressGroup.add(memAddressDecItem);

    // Create Memory Contents menu and put it in format menu
		JMenu memContentsMenu = new JMenu("Memory Contents");
  	memContentsMenu.setToolTipText(
		    "Change the numeric representation of the memory contents");

		JMenuItem memContentsHexItem = new JCheckBoxMenuItem("Hexadecimal", true);
		memContentsHexItem.setActionCommand("mem contents-hex");
		memContentsHexItem.addActionListener(this);
		memContentsMenu.add(memContentsHexItem);
		JMenuItem memContentsDecItem = new JCheckBoxMenuItem("Unsigned Decimal");
		memContentsDecItem.setActionCommand("mem contents-dec");
		memContentsDecItem.addActionListener(this);
		memContentsMenu.add(memContentsDecItem);
    formatMenu.add(memContentsMenu);
		JMenuItem memContentsSignedDecItem = new JCheckBoxMenuItem("Signed Decimal");
		memContentsSignedDecItem.setActionCommand("mem contents-signed-dec");
		memContentsSignedDecItem.addActionListener(this);
		memContentsMenu.add(memContentsSignedDecItem);
    formatMenu.add(memContentsMenu);

    // Memory Contents button group
		ButtonGroup memContentsGroup = new ButtonGroup();
		memContentsGroup.add(memContentsHexItem);
		memContentsGroup.add(memContentsDecItem);
		memContentsGroup.add(memContentsSignedDecItem);


		// Clear Menu
		clearMenu = new JMenu("Clear");
    clearMenu.setOpaque(false);
		mb.add(clearMenu);
    mb.add(new MenuPadding());

		JMenuItem clearRegItem = new JMenuItem("Clear Registers");
		clearRegItem.setActionCommand("clear registers");
		clearRegItem.addActionListener(this);
  	clearRegItem.setToolTipText("Zero-out the registers");
		clearMenu.add(clearRegItem);
		
		clearMemItem = new JMenuItem("Clear Memory");
		clearMemItem.setActionCommand("clear mem");
		clearMemItem.addActionListener(this);
  	clearMemItem.setToolTipText("Zero-out memory");
		clearMenu.add(clearMemItem);
		
		JMenuItem clearTraceItem = new JMenuItem("Clear Trace");
		clearTraceItem.setActionCommand("clear trace");
		clearTraceItem.addActionListener(this);
  	clearTraceItem.setToolTipText("Erase the instruction trace window");
		clearMenu.add(clearTraceItem);
		
		JMenuItem clearAllItem = new JMenuItem("Clear Everything");
		clearAllItem.setActionCommand("clear everything");
		clearAllItem.addActionListener(this);
  	clearAllItem.setToolTipText(
		  "Clear the registers, memory, instruction trace, console lights, " +
			"and debug number fields");
		clearMenu.add(clearAllItem);
		

		// Options menu
		JMenu optionsMenu = new JMenu("Options");
    optionsMenu.setOpaque(false);
		mb.add(optionsMenu);
    mb.add(new MenuPadding());
		
		clearPromptItem = new JCheckBoxMenuItem("Disable Clear Prompts", false);
  	clearPromptItem.setToolTipText(
		    "Disable the YES/NO prompts displayed when clearing the " +
				                 "registers, memory, or instruction trace");
		optionsMenu.add(clearPromptItem);

		JCheckBoxMenuItem toolTipsItem = new JCheckBoxMenuItem("Console Help Mode");
		toolTipsItem.setActionCommand("disable tool tips");
		toolTipsItem.addActionListener(this);
  	toolTipsItem.setToolTipText("Turn on these handy little tips");
		optionsMenu.add(toolTipsItem);

		// Background menu
		JMenu backgroundMenu = new JMenu("Background");
  	backgroundMenu.setToolTipText("Change the background pattern");
		optionsMenu.add(backgroundMenu);

		JCheckBoxMenuItem lightMetalItem = new JCheckBoxMenuItem("Light Metal");
		lightMetalItem.setActionCommand("light metal");
		lightMetalItem.addActionListener(backgroundChanger);
		backgroundMenu.add(lightMetalItem);

		metalItem = new JCheckBoxMenuItem("Metal");
		metalItem.setActionCommand("metal");
		metalItem.addActionListener(backgroundChanger);
		backgroundMenu.add(metalItem);

		JCheckBoxMenuItem darkMetalItem = new JCheckBoxMenuItem("Dark Metal");
		darkMetalItem.setActionCommand("dark metal");
		darkMetalItem.addActionListener(backgroundChanger);
		backgroundMenu.add(darkMetalItem);

		JCheckBoxMenuItem cardboardItem = new JCheckBoxMenuItem("Cardboard");
		cardboardItem.setActionCommand("cardboard");
		cardboardItem.addActionListener(backgroundChanger);
		backgroundMenu.add(cardboardItem);

		JCheckBoxMenuItem corkItem = new JCheckBoxMenuItem("Cork");
		corkItem.setActionCommand("cork");
		corkItem.addActionListener(backgroundChanger);
		backgroundMenu.add(corkItem);

		JCheckBoxMenuItem woodItem = new JCheckBoxMenuItem("Wood");
		woodItem.setActionCommand("wood");
		woodItem.addActionListener(backgroundChanger);
		backgroundMenu.add(woodItem);

		JCheckBoxMenuItem marbleItem = new JCheckBoxMenuItem("Marble");
		marbleItem.setActionCommand("marble");
		marbleItem.addActionListener(backgroundChanger);
		backgroundMenu.add(marbleItem);

		JCheckBoxMenuItem paperItem = new JCheckBoxMenuItem("Paper");
		paperItem.setActionCommand("paper");
		paperItem.addActionListener(backgroundChanger);
		backgroundMenu.add(paperItem);

		JCheckBoxMenuItem parchmentItem = new JCheckBoxMenuItem("Parchment");
		parchmentItem.setActionCommand("parchment");
		parchmentItem.addActionListener(backgroundChanger);
		backgroundMenu.add(parchmentItem);

		JCheckBoxMenuItem boringGrayItem = new JCheckBoxMenuItem("Boring Gray");
		boringGrayItem.setActionCommand("boring gray");
		boringGrayItem.addActionListener(backgroundChanger);
		backgroundMenu.add(boringGrayItem);
		
    // Background button group
		ButtonGroup backgroundGroup = new ButtonGroup();
		backgroundGroup.add(lightMetalItem);
		backgroundGroup.add(metalItem);
		backgroundGroup.add(darkMetalItem);
		backgroundGroup.add(cardboardItem);
		backgroundGroup.add(corkItem);
		backgroundGroup.add(woodItem);
		backgroundGroup.add(marbleItem);
		backgroundGroup.add(paperItem);
		backgroundGroup.add(parchmentItem);
		backgroundGroup.add(boringGrayItem);
		
		//I/O Device Menu
		ioDeviceMenu = new JMenu("I/O Devices");
    ioDeviceMenu.setOpaque(false);
		mb.add(ioDeviceMenu);
    mb.add(new MenuPadding());
		
		JMenuItem calcItem = new JMenuItem("Calculator");
		calcItem.setToolTipText(
		     "Run Memory-Mapped Calc Program");
		calcItem.setActionCommand("run calc");
		calcItem.addActionListener(this);
		ioDeviceMenu.add(calcItem);

		return mb;
	}  // End of createMenuBar




  /* Disables (or enables) all menu items and buttons whose 
	   actions could improperly affect the state of the simulator 
		 when it is executing. */
	public void setEnabledConsoleButtons(boolean enable)
	{
	  fileMenu.setEnabled(enable);
 		formatMenu.setEnabled(enable);
 		clearMenu.setEnabled(enable);
	  tscConsole.setEnabledButtons(enable);
 		debugConsole.setEnabledButtons(enable);
	  ioConsole.setEnabledButtons(enable);
		ioDeviceMenu.setEnabled(enable);
	} // End of setEnabledConsoleButtons




  /* Updates the input file name on the file menu and 
	   enables/disables the 'reset input file' and 'close 
		 input file' menu items. */
  public void updateInputFileMenu(String newInputFileName)
	{
    if (newInputFileName != null)  {
      inputFileLabel.setText("      Input File:  " + newInputFileName + "     ");
      resetInputFileItem.setEnabled(true);
      closeInputFileItem.setEnabled(true);
		}
		else  {
      inputFileLabel.setText("      Input File:  none     ");
      resetInputFileItem.setEnabled(false);
      closeInputFileItem.setEnabled(false);
		}
  }  // End of updateInputFileMenu




  /* Updates the output file name on the file menu and 
	   enables (disables) the 'reset output file' and 'close 
		 output file' menu items. */
  public void updateOutputFileMenu(String newOutputFileName)
	{
    if (newOutputFileName != null)  {
      outputFileLabel.setText("      Output File:  " + newOutputFileName + "     ");
      resetOutputFileItem.setEnabled(true);
      closeOutputFileItem.setEnabled(true);
		}
		else  {
      outputFileLabel.setText("      Output File:  none      ");
      resetOutputFileItem.setEnabled(false);
      closeOutputFileItem.setEnabled(false);
		}
  }  // End of updateOutputFileMenu




  // Responds to all button events in the menu bar
  public void actionPerformed(ActionEvent e)
	{
    String ac = e.getActionCommand();
		
    if (ac.equals("exit"))  {
			if (new YesNoDialog(this).show("Exit TSC", "Exit TSC - Are you sure?")
			                                                   == YesNoDialog.YES)  {
			  System.exit(0);
			  ioConsole.closeInputFile();
			  ioConsole.closeOutputFile();
			}
		}

		else if (ac.equals("load rom"))  {
		  File file = fileTools.openFile(this, "ROM", "Load From A ROM");
      if (file != null)
			  mem.loadRom(file);
 	  }

		else if (ac.equals("burn rom"))  {
		  File file = fileTools.saveRomFile(this);
      if (file != null) 
			  mem.makeRom(file);
 	  }

		else if (ac.equals("select input file"))  {
		  File file = fileTools.openFile(this, "", "Input File");
      if (file != null)
			  if (ioConsole.setInputFile(file))
					updateInputFileMenu(file.getName());
 	  }

		else if (ac.equals("select output file"))  {
		  File file = fileTools.saveFile(this, "", "Output File");
      if (file != null)
			  if (ioConsole.setOutputFile(file))
					updateOutputFileMenu(file.getName());
 	  }

		else if (ac.equals("reset input file"))  {
		  ioConsole.resetInputFile();
 	  }

		else if (ac.equals("reset output file"))  {
		  ioConsole.resetOutputFile();
 	  }

		else if (ac.equals("close input file"))  {
		  ioConsole.closeInputFile();
 	  }

		else if (ac.equals("close output file"))  {
		  ioConsole.closeOutputFile();
 	  }

		else if (ac.equals("pc-hex"))  {
			regs.updateRegFormat(PC, HEX);
		}

		else if (ac.equals("pc-dec"))  {
			regs.updateRegFormat(PC, DECIMAL);
		}

		else if (ac.equals("pc-binary"))  {
			regs.updateRegFormat(PC, BINARY);
		}

		else if (ac.equals("gpr0-hex"))  {
			regs.updateRegFormat(GPR0, HEX);
		}

		else if (ac.equals("gpr0-dec"))  {
			regs.updateRegFormat(GPR0, DECIMAL);
		}

		else if (ac.equals("gpr0-signed-dec"))  {
			regs.updateRegFormat(GPR0, SIGNED_DECIMAL);
		}

		else if (ac.equals("gpr0-binary"))  {
			regs.updateRegFormat(GPR0, BINARY);
		}

		else if (ac.equals("gpr1-hex"))  {
			regs.updateRegFormat(GPR1, HEX);
		}

		else if (ac.equals("gpr1-dec"))  {
			regs.updateRegFormat(GPR1, DECIMAL);
		}

		else if (ac.equals("gpr1-signed-dec"))  {
			regs.updateRegFormat(GPR1, SIGNED_DECIMAL);
		}

		else if (ac.equals("gpr1-binary"))  {
			regs.updateRegFormat(GPR1, BINARY);
		}

		else if (ac.equals("gpr2-hex"))  {
			regs.updateRegFormat(GPR2, HEX);
		}

		else if (ac.equals("gpr2-dec"))  {
			regs.updateRegFormat(GPR2, DECIMAL);
		}

		else if (ac.equals("gpr2-signed-dec"))  {
			regs.updateRegFormat(GPR2, SIGNED_DECIMAL);
		}

		else if (ac.equals("gpr2-binary"))  {
			regs.updateRegFormat(GPR2, BINARY);
		}

		else if (ac.equals("gpr3-hex"))  {
			regs.updateRegFormat(GPR3, HEX);
		}

		else if (ac.equals("gpr3-dec"))  {
			regs.updateRegFormat(GPR3, DECIMAL);
		}

		else if (ac.equals("gpr3-signed-dec"))  {
			regs.updateRegFormat(GPR3, SIGNED_DECIMAL);
		}

		else if (ac.equals("gpr3-binary"))  {
			regs.updateRegFormat(GPR3, BINARY);
		}

		else if (ac.equals("allGPR-hex"))  {
      gpr0HexItem.doClick();
      gpr1HexItem.doClick();
      gpr2HexItem.doClick();
      gpr3HexItem.doClick();
    }

		else if (ac.equals("allGPR-dec"))  {
      gpr0DecItem.doClick();
      gpr1DecItem.doClick();
      gpr2DecItem.doClick();
      gpr3DecItem.doClick();
		}

		else if (ac.equals("allGPR-signed-dec"))  {
      gpr0SignedDecItem.doClick();
      gpr1SignedDecItem.doClick();
      gpr2SignedDecItem.doClick();
      gpr3SignedDecItem.doClick();
		}

		else if (ac.equals("allGPR-binary"))  {
      gpr0BinaryItem.doClick();
      gpr1BinaryItem.doClick();
      gpr2BinaryItem.doClick();
      gpr3BinaryItem.doClick();
		}

		else if (ac.equals("mem address-hex"))  {
			debugConsole.setPCFormat(HEX);
			mem.updateMemAddressFormat(HEX);
		}

		else if (ac.equals("mem address-dec"))  {
			debugConsole.setPCFormat(DECIMAL);
			mem.updateMemAddressFormat(DECIMAL);
		}

		else if (ac.equals("mem contents-hex"))  {
			mem.updateMemContentsFormat(HEX);
		}

		else if (ac.equals("mem contents-dec"))  {
			mem.updateMemContentsFormat(DECIMAL);
		}

		else if (ac.equals("mem contents-signed-dec"))  {
			mem.updateMemContentsFormat(SIGNED_DECIMAL);
		}

		else if (ac.equals("clear registers"))  {
      tscConsole.clearButton.doClick();
		}
		
		else if (ac.equals("clear mem"))  {
		  if (!clearPromptItem.isSelected())  {
				if (new YesNoDialog(this).show("Clear Memory",
				    "Clear Memory - Are you sure?") == YesNoDialog.YES)  {
					mem.clearMem();
				}
			}
			else  mem.clearMem();
		}
		
		else if (ac.equals("clear trace"))  {
      debugConsole.clearTraceButton.doClick();
		}
		
		else if (ac.equals("clear everything"))  {
			if (new YesNoDialog(this).show(
			   "Clear Everything", "Clear Everything - Are you sure?")
			                                       == YesNoDialog.YES)  {
        boolean oldClearPrompt = clearPromptItem.isSelected();
        clearPromptItem.setSelected(true);

				tscConsole.setLights(0);
				ioConsole.setLights(0);
        debugConsole.clearTextFields();
        tscConsole.clearButton.doClick();
        debugConsole.clearTraceButton.doClick();
        clearMemItem.doClick();
				
        clearPromptItem.setSelected(oldClearPrompt);				
			}
		}
		
		else if (ac.equals("disable tool tips"))  {
			JCheckBoxMenuItem cb = (JCheckBoxMenuItem) e.getSource();
			if(cb.isSelected())
	      ToolTipManager.sharedInstance().setEnabled(true);
			else
	      ToolTipManager.sharedInstance().setEnabled(false);
		}

		else if (ac.equals("credits"))  {
		  String message[] = new String[7];
			message[0] = "";
			message[1] = "TSC Simulator";
			message[2] = "";
			message[3] = "Devolped by Jeff Penfold for CS 143";
			message[4] = "Brigham Young University";
			message[5] = "Copyright 1999";
			message[6] = "";

      new OkDialog(this, true).show("About TSC Simulator", message);
		}
		
		else if(ac.equals("run calc")){
			//Calling Calc GUI
			if(!calcRunning)
			{
				calc = new Calc(mem, this);
				calc.setTitle("TSC Calculator");	
  	  	calc.pack();
				calc.setLocation(525, 450);  // Move gui away from the corner - over to I/O console area			
    		calc.show();
			}
			else
				calc.requestFocus();
		}
    
		// Repaint the menu to work around a repainting bug in JMenu in JDK 1.2.2
    repaint();

  }  // End of actionPerformed




	/* This utility class simply changes the backgrounds of the 
	   three consoles.  It is in its own class because the 
		 'actionPerformed' method above was getting too big.  */
	private class BckgrdChngr implements ActionListener
	{
	  // Responds to all background change events
	  public void actionPerformed(ActionEvent e)
		{
	    String ac = e.getActionCommand();
  		ImageIcon wallpaper = null;
			Color color = null;
			
	    if (ac.equals("boring gray"))  {
				((JPanel) getContentPane()).setBackground(Constants.BORING_GRAY_1);
	      mb.setBackground(Constants.BORING_GRAY_1);
				tscConsole.setWallpaper(null, Constants.BORING_GRAY_2);
				debugConsole.setWallpaper(null, Constants.BORING_GRAY_2);
				ioConsole.setWallpaper(null, Constants.BORING_GRAY_2);
				return;
			}

			else if (ac.equals("light metal"))  {
			  color = Constants.BORING_GRAY_1;
				wallpaper = new ImageIcon("images/metal-light.jpg");
      }

      else if (ac.equals("metal"))  {
			  color = new Color(210, 210, 210);
				wallpaper = new ImageIcon("images/metal.jpg");
      }

			else if (ac.equals("dark metal"))  {
			  color = new Color(194, 194, 194);
				wallpaper = new ImageIcon("images/metal-dark.jpg");
      }

			else if (ac.equals("cardboard"))  {
			  color = new Color(222, 206, 179);
				wallpaper = new ImageIcon("images/cardboard.jpg");
      }

			else if (ac.equals("cork"))  {
			  color = new Color(212, 199, 180);
				wallpaper = new ImageIcon("images/cork.jpg");
      }

			else if (ac.equals("wood"))  {
			  color = new Color(238, 231, 210);
				wallpaper = new ImageIcon("images/wood.jpg");
      }

			else if (ac.equals("marble"))  {
			  color = new Color(194, 194, 194);
				wallpaper = new ImageIcon("images/marble.jpg");
      }

			else if (ac.equals("paper"))  {
			  color = new Color(204, 200, 200);
				wallpaper = new ImageIcon("images/paper.gif");
      }

			else if (ac.equals("parchment"))  {
			  color = new Color(236, 210, 172);
				wallpaper = new ImageIcon("images/parchment.gif");
      }


  		// Set the window and menu color
			((JPanel) getContentPane()).setBackground(color);
      mb.setBackground(color);

			// Send the wallpaper and border color to each console
			tscConsole.setWallpaper(wallpaper, color);
			debugConsole.setWallpaper(wallpaper, color);
			ioConsole.setWallpaper(wallpaper, color);
		  
			// Repaint (duh...)
			repaint();
		
		}  // End of actionPerformed
	}  // End of BckgrdChngr class
	
	
	
	
	/* This class provides a fixed size invisible separator 
	   to put between menu headings on the menu bar.  */
	private class MenuPadding extends JSeparator
	{
	  public MenuPadding()
		{
		  super(SwingConstants.VERTICAL);
			setMaximumSize(new Dimension(25, 21));
			setMinimumSize(new Dimension(25, 21));
			setVisible(false);
		}  // End of constructor
	}  // End of MenuPadding class

}  // End of Display class

