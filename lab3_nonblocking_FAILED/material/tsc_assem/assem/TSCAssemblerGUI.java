/*
© 2000, Performance Evaluation Laboratory, Brigham Young University. Reproduction of all or part of this work is permitted for non-profit educational
or research use provided this copyright notice remains intact. All other rights reserved. 
*/

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;


/* The GUI for the TSC Assembler.  This GUI also provides access to 
   other tools i.e. a binary file viewer, a memory map viewer, and a
	 .bin to .hex converter (.hex files are used by the Logic Works
	 CAD tool to initialize memory chips.) 
*/


class TSCAssemblerGUI extends JFrame implements ActionListener,
                                                CaretListener,
                                                DocumentListener
{
  // Constants
  private static final String INITIAL_FILENAME = " ";
  private static final int FONTSTYLE = 0;
	private static final int FONTSIZE = 15;
	private static final int MAX_TAB_SIZE = 10;
	private static final int MIN_TAB_SIZE = 1;
	private static final int DEFAULT_TAB_SIZE = 4;
  final static String NEWLINE = System.getProperty("line.separator");
	
	// An array of strings representing tabs.  The index of each string 
  // is the number of spaces in the string.  The array should always
  // hold MAX_TAB_SIZE + 1 strings.
	private final static String tabString[] = new String[]  {
	  "", " ", "  ", "   ", "    ", "     ", "      ", "       ", 
		"        ", "         ", "          "  };
	
	// Local vars
	private JScrollPane outputScrollPane;
  private JTextArea outputArea;
	private JTextArea editorArea;
	private JLabel lineLabel;
  private JLabel filenameLabel;
	private JMenuItem closeItem;
	private JMenuItem saveItem;
	private JMenuItem saveAsItem;
	private JMenuItem cutItem;
	private JMenuItem copyItem;
	private JMenuItem pasteItem;
	private JMenuItem assemblerItem;
	private JMenuItem binToHexItem;
	private JMenuItem memMapViewerItem;
	private JMenuItem binFileViewerItem;
	private JCheckBoxMenuItem wordWrapItem;
	private JCheckBoxMenuItem saveBeforeAssembleItem;
	private JMenuItem showSymTableItem;
	private JMenuItem showSegmentInfoItem;
	private JMenuItem incTabSizeItem;
	private JMenuItem decTabSizeItem;
  private JButton saveButton;
  private JButton cutButton;
  private JButton copyButton;
  private JButton pasteButton;
  private JButton assembleButton;
	private JButton viewBinFileButton;
	private JButton viewMemMapButton;
	private JButton binToHexButton;

  private TSCAssembler tscAssembler;
	private FileTools fileTools;
	private EnsrVis ensureOutputIsVisible;
	
	private File tscFile;         // The TSC code file
	private boolean docChanged;   // True if the document needs to be saved
  private int tabSize;          // The tab size



	public TSCAssemblerGUI(TSCAssembler tscAssembler)
	{
	  this.tscAssembler = tscAssembler;
    docChanged = false;
		tabSize = DEFAULT_TAB_SIZE;
		
    // Set the output stream of the assembler to use the output text area
		tscAssembler.setOutputStream(new PrintStream(
		                          new TxtOutput(), true));

    // Initialize the ensureOutputIsVisible, fileTools, and tscFile classes
		ensureOutputIsVisible = new EnsrVis();
		fileTools = new FileTools(null);
		tscFile = null;

    // Initialize the frame.  It consists of a menu, a main panel, and a 
		// line number label.
		getContentPane().setLayout(new GridBagLayout());
    setTitle("TSC Assembler and Other Tools");
	  GridBagConstraints gbc = new GridBagConstraints();


		// Create menu bar and add menu items to it.
    JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

    // File Menu
		JMenu fileMenu = new JMenu("File");
    menuBar.add(new MnuPad());
		menuBar.add(fileMenu);
    menuBar.add(new MnuPad());
		
		// Create the new file menu item
		JMenuItem newItem = new JMenuItem("New...");
  	newItem.setAccelerator(
		    KeyStroke.getKeyStroke(KeyEvent.VK_N, Event.CTRL_MASK));
		newItem.setActionCommand("new");
		newItem.addActionListener(this);
		fileMenu.add(newItem);

		// Create the open file menu item
		JMenuItem openItem = new JMenuItem("Open...");
  	openItem.setAccelerator(
		    KeyStroke.getKeyStroke(KeyEvent.VK_O, Event.CTRL_MASK));
		openItem.setActionCommand("open");
		openItem.addActionListener(this);
		fileMenu.add(openItem);

		// Create the close file menu item
		closeItem = new JMenuItem("Close");
		closeItem.setActionCommand("close");
		closeItem.addActionListener(this);
		fileMenu.add(closeItem);

		// Create the save file menu item
		saveItem = new JMenuItem("Save");
  	saveItem.setAccelerator(
		    KeyStroke.getKeyStroke(KeyEvent.VK_S, Event.CTRL_MASK));
		saveItem.setActionCommand("save");
		saveItem.addActionListener(this);
		fileMenu.add(saveItem);

		// Create the saveAs file menu item
		saveAsItem = new JMenuItem("Save As...");
		saveAsItem.setActionCommand("save as");
		saveAsItem.addActionListener(this);
		fileMenu.add(saveAsItem);

		// Create the exit menu item
		JMenuItem exitItem = new JMenuItem("Exit");
		exitItem.setActionCommand("exit");
		exitItem.addActionListener(this);
		fileMenu.addSeparator();
		fileMenu.add(exitItem);

    
		// Edit Menu
		JMenu editMenu = new JMenu("Edit");
		menuBar.add(editMenu);
    menuBar.add(new MnuPad());

		// Create the cut menu item
		cutItem = new JMenuItem("Cut");
  	cutItem.setAccelerator(
		    KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, Event.SHIFT_MASK));
		cutItem.setActionCommand("cut");
		cutItem.addActionListener(this);
		editMenu.add(cutItem);

		// Create the copy menu item
		copyItem = new JMenuItem("Copy");
  	copyItem.setAccelerator(
		    KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, Event.CTRL_MASK));
		copyItem.setActionCommand("copy");
		copyItem.addActionListener(this);
		editMenu.add(copyItem);

		// Create the paste menu item
		pasteItem = new JMenuItem("Paste");
  	pasteItem.setAccelerator(
		    KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, Event.SHIFT_MASK));
		pasteItem.setActionCommand("paste");
		pasteItem.addActionListener(this);
		editMenu.add(pasteItem);

		
		// Tools Menu
		JMenu toolsMenu = new JMenu("Tools");
		menuBar.add(toolsMenu);
    menuBar.add(new MnuPad());

		// Create the assembler menu item
		assemblerItem = new JMenuItem("Assembler");
  	assemblerItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
		assemblerItem.setActionCommand("assembler");
		assemblerItem.addActionListener(this);
		toolsMenu.add(assemblerItem);

		// Create the memory map viewer menu item
		memMapViewerItem = new JMenuItem("Memory Map Viewer");
  	memMapViewerItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
		memMapViewerItem.setActionCommand("memory map viewer");
		memMapViewerItem.addActionListener(this);
		toolsMenu.add(memMapViewerItem);

		// Create the view binary file menu item
		binFileViewerItem = new JMenuItem("Binary File Viewer");
  	binFileViewerItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));
		binFileViewerItem.setActionCommand("binary file viewer");
		binFileViewerItem.addActionListener(this);
		toolsMenu.add(binFileViewerItem);

		// Create the bin to hex menu item
		binToHexItem = new JMenuItem(".bin-to-.hex Converter");
  	binToHexItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0));
		binToHexItem.setActionCommand("bin to hex");
		binToHexItem.addActionListener(this);
		toolsMenu.add(binToHexItem);

		// Create the clear output area menu item
		JMenuItem clearItem = new JMenuItem("Clear Tool Output Window");
  	clearItem.setAccelerator(
		    KeyStroke.getKeyStroke(KeyEvent.VK_E, Event.CTRL_MASK));
		clearItem.setActionCommand("clear output");
		clearItem.addActionListener(this);
		toolsMenu.addSeparator();
		toolsMenu.add(clearItem);


		// Configure Menu
		JMenu configureMenu = new JMenu("Configure");
		menuBar.add(configureMenu);

		// Create the word wrap menu item
		wordWrapItem = new JCheckBoxMenuItem(
		                         "Word Wrap Long Lines", false);
  	wordWrapItem.setAccelerator(
		    KeyStroke.getKeyStroke(KeyEvent.VK_W, Event.CTRL_MASK));
		wordWrapItem.setActionCommand("word wrap");
		wordWrapItem.addActionListener(this);
		configureMenu.add(wordWrapItem);

		// Create the save before assembling menu item
		saveBeforeAssembleItem = new JCheckBoxMenuItem(
		                "Save Before Assembling", true);
    saveBeforeAssembleItem.setToolTipText(
		    "Save the current document before executing the assembler");
		configureMenu.add(saveBeforeAssembleItem);

		// Create assembler options sub-menu
		JMenu assemblerOptionsMenu = new JMenu("Assembler Command Line Options");
		configureMenu.add(assemblerOptionsMenu);
		
		// Create the show segment info menu item and add it to the sub-menu
		showSegmentInfoItem = new JCheckBoxMenuItem(
		           "Show Segment Information", true);
		showSegmentInfoItem.setAccelerator(
		    KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0));
    showSegmentInfoItem.setToolTipText(
		    "Show the segment information when a file is assembled");
		assemblerOptionsMenu.add(showSegmentInfoItem);

		// Create the show symbol table menu item and add it to the sub-menu
		showSymTableItem = new JCheckBoxMenuItem("Show Symbol Table", false);
  	showSymTableItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0));
    showSymTableItem.setToolTipText(
		    "Show the symbol table when a file is assembled");
		assemblerOptionsMenu.add(showSymTableItem);

		// Create the change tab size menu items
		incTabSizeItem = new JMenuItem("Increase Tab Size to " + (tabSize + 1));
		incTabSizeItem.setActionCommand("inc tab size");
  	incTabSizeItem.setAccelerator(
		    KeyStroke.getKeyStroke(KeyEvent.VK_CLOSE_BRACKET, Event.CTRL_MASK));
		incTabSizeItem.addActionListener(this);
		configureMenu.add(incTabSizeItem);
		
		decTabSizeItem = new JMenuItem("Decrease Tab Size to " + (tabSize - 1));
  	decTabSizeItem.setAccelerator(
		    KeyStroke.getKeyStroke(KeyEvent.VK_OPEN_BRACKET, Event.CTRL_MASK));
		decTabSizeItem.setActionCommand("dec tab size");
		decTabSizeItem.addActionListener(this);
		configureMenu.add(decTabSizeItem);

    
		/* Create main panel.  This panel contains the tool bar and the split 
		   pane.  The sole purpose of this panel is to allow the toolbar to 
			 be dragged to either the top or bottom of the panel (user's 
			 discretion).  The line number label at the bottom of the main 
			 window prevents the tool bar from being placed there so it has to 
			 have it's own panel.  */
    JPanel mainPanel = new JPanel(new BorderLayout());
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1;  gbc.weighty = 1;
		getContentPane().add(mainPanel, gbc);

    // Create the tool bar and add it to the main panel
		JToolBar toolBar = new JToolBar();
		mainPanel.add(toolBar, BorderLayout.NORTH);
		
		// Create the new file button
		JButton newButton = new JButton(new ImageIcon("images/new2.gif"));
		newButton.setFocusPainted(false);
		newButton.setActionCommand("new");
		newButton.addActionListener(this);
		newButton.setAlignmentY(CENTER_ALIGNMENT);
    newButton.setToolTipText("New");
		toolBar.addSeparator();
		toolBar.add(newButton);

		// Create the open file button
		JButton openButton = new JButton(new ImageIcon("images/open2.gif"));
		openButton.setFocusPainted(false);
		openButton.setActionCommand("open");
		openButton.addActionListener(this);
		openButton.setAlignmentY(CENTER_ALIGNMENT);
    openButton.setToolTipText("Open");
		toolBar.add(openButton);
    
		// Create the save file button
		saveButton = new JButton(new ImageIcon("images/save2.gif"));
		saveButton.setFocusPainted(false);
		saveButton.setActionCommand("save");
		saveButton.addActionListener(this);
		saveButton.setAlignmentY(CENTER_ALIGNMENT);
    saveButton.setToolTipText("Save");
		toolBar.add(saveButton);
		toolBar.addSeparator();
    
		// Create the cut file button
		cutButton = new JButton(new ImageIcon("images/cut2.gif"));
		cutButton.setFocusPainted(false);
		cutButton.setActionCommand("cut");
		cutButton.addActionListener(this);
		cutButton.setAlignmentY(CENTER_ALIGNMENT);
    cutButton.setToolTipText("Cut");
		toolBar.add(cutButton);
    
		// Create the copy file button
		copyButton = new JButton(new ImageIcon("images/copy2.gif"));
		copyButton.setFocusPainted(false);
		copyButton.setActionCommand("copy");
		copyButton.addActionListener(this);
		copyButton.setAlignmentY(CENTER_ALIGNMENT);
    copyButton.setToolTipText("Copy");
		toolBar.add(copyButton);
    
		// Create the paste file button
		pasteButton = new JButton(new ImageIcon("images/paste2.gif"));
		pasteButton.setFocusPainted(false);
		pasteButton.setActionCommand("paste");
		pasteButton.addActionListener(this);
		pasteButton.setAlignmentY(CENTER_ALIGNMENT);
    pasteButton.setToolTipText("Paste");
		toolBar.add(pasteButton);
		toolBar.addSeparator();
    
		// Create the assemble button
		assembleButton = new JButton(new ImageIcon("images/assemble_small.gif"));
		assembleButton.setFocusPainted(false);
		assembleButton.setActionCommand("assembler");
		assembleButton.addActionListener(this);
		assembleButton.setAlignmentY(CENTER_ALIGNMENT);
		toolBar.add(assembleButton);

    // Create the view memory map button
		viewMemMapButton = new JButton(new ImageIcon("images/ram_small.gif"));
		viewMemMapButton.setFocusPainted(false);
		viewMemMapButton.setActionCommand("memory map viewer");
		viewMemMapButton.addActionListener(this);
		viewMemMapButton.setAlignmentY(CENTER_ALIGNMENT);
		toolBar.add(viewMemMapButton);

    // Create the view binary file button
		viewBinFileButton = new JButton(new ImageIcon("images/face4_small.gif"));
		viewBinFileButton.setFocusPainted(false);
		viewBinFileButton.setActionCommand("binary file viewer");
		viewBinFileButton.addActionListener(this);
		viewBinFileButton.setAlignmentY(CENTER_ALIGNMENT);
		toolBar.add(viewBinFileButton);
		
    // Create the bin to hex conversion button
		binToHexButton = new JButton(new ImageIcon("images/convert_small.gif"));
		binToHexButton.setFocusPainted(false);
		binToHexButton.setActionCommand("bin to hex");
		binToHexButton.addActionListener(this);
		binToHexButton.setAlignmentY(CENTER_ALIGNMENT);
		toolBar.add(binToHexButton);
		toolBar.addSeparator();
		
    // Create the clear output area button
		JButton clearButton = new JButton(
		    new ImageIcon("images/sign_dump_small.gif"));
		clearButton.setFocusPainted(false);
		clearButton.setActionCommand("clear output");
		clearButton.addActionListener(this);
		clearButton.setAlignmentY(CENTER_ALIGNMENT);
    clearButton.setToolTipText("Dump (clear) the tool output window");
		toolBar.add(clearButton);
		toolBar.addSeparator();


    // Create the editor area
		editorArea = new JTextArea();
		editorArea.setFont(new Font("Monospaced", FONTSTYLE, FONTSIZE));
		editorArea.setMargin(new Insets(0, 8, 0, 8));
		editorArea.setWrapStyleWord(true);
		editorArea.addCaretListener(this);
		editorArea.getDocument().addDocumentListener(this);
		JScrollPane editorScrollPane = new JScrollPane(editorArea);
		
		// Add key bindings to the editor area
		TabAction tabAction = new TabAction();
    editorArea.getKeymap().addActionForKeyStroke(
		    KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), tabAction);
    editorArea.getKeymap().addActionForKeyStroke(
		    KeyStroke.getKeyStroke(KeyEvent.VK_TAB, Event.SHIFT_MASK), tabAction);
    
    // Create the text output area
		outputArea = new JTextArea();
		outputArea.setEditable(false);
		outputArea.setFont(new Font("Monospaced", FONTSTYLE, FONTSIZE));
		outputArea.setMargin(new Insets(0, 8, 0, 8));
		outputScrollPane = new JScrollPane(outputArea);

		// Create the split pane and add the editor and text output areas to it
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                                 editorScrollPane, outputScrollPane);
		splitPane.setPreferredSize(new Dimension(700, 600));
    splitPane.setContinuousLayout(true);
		splitPane.setOneTouchExpandable(true);
		mainPanel.add(splitPane, BorderLayout.CENTER);


    // Create the filename label and add it to the frame
		filenameLabel = new JLabel(INITIAL_FILENAME);
		filenameLabel.setAlignmentY(CENTER_ALIGNMENT);
		filenameLabel.setForeground(Color.black);
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0;  gbc.weighty = 0;
		gbc.insets = new Insets(0, 20, 0, 20);
		getContentPane().add(filenameLabel, gbc);

    // Create the line number label and add it to the frame.
		lineLabel = new JLabel("Line 1     Column 1", JLabel.LEFT);
		  // Make the width of the label long enough that it's left edge 
			// remains stationary as the width of the line number grows.
			// The height is determined by the font.
		Dimension minSize = new Dimension(160, 
		    lineLabel.getFontMetrics(lineLabel.getFont()).getHeight() + 2);
		lineLabel.setMinimumSize(minSize);
		lineLabel.setPreferredSize(minSize);
		lineLabel.setForeground(Color.black);
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0, 0, 0, 20);
		getContentPane().add(lineLabel, gbc);

		
    // Respond to WindowClosing event
		addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
	  });

		// Pack the components, position the split pane divider, and set 
		// some stuff
		pack();
  	splitPane.setDividerLocation(0.65);
		setStuff(false);

	}  // End of constructor




  /* Enable (disable) buttons and menu items, repaint the editor area 
	   background and make it editable (non-editable), set (erase) the 
		 filename label, and update the tool tips. */
	private void setStuff(boolean enable)
	{
		closeItem.setEnabled(enable);
		saveItem.setEnabled(enable);
		saveAsItem.setEnabled(enable);
		cutItem.setEnabled(enable);
		copyItem.setEnabled(enable);
		pasteItem.setEnabled(enable);
		assemblerItem.setEnabled(enable);
		binToHexItem.setEnabled(enable);
		memMapViewerItem.setEnabled(enable);
		binFileViewerItem.setEnabled(enable);
		saveButton.setEnabled(enable);
		cutButton.setEnabled(enable);
		copyButton.setEnabled(enable);
		pasteButton.setEnabled(enable);
		assembleButton.setEnabled(enable);
		viewMemMapButton.setEnabled(enable);
		viewBinFileButton.setEnabled(enable);
		binToHexButton.setEnabled(enable);
	  editorArea.setEnabled(enable);

		if (enable)  {
		  String filename = tscFile.getName().trim();
			String binFilename = FileTools.replaceExtension(filename, "bin");

		  filenameLabel.setText(tscFile.getAbsolutePath().trim());
			editorArea.setBackground(Color.white);
	    assembleButton.setToolTipText("Assemble " + filename);
	    viewMemMapButton.setToolTipText("Show the memory map of " + 
			                                                binFilename);
	    viewBinFileButton.setToolTipText("View the contents of " + 
			                                                     binFilename);
	    binToHexButton.setToolTipText("Convert " + binFilename + " to "
			                 + FileTools.replaceExtension(filename, "hex"));
		}
		else  {
		  tscFile = null;
		  filenameLabel.setText(INITIAL_FILENAME);
			editorArea.setBackground(Color.gray);
	    assembleButton.setToolTipText("Assemble a TSC code file");
	    viewMemMapButton.setToolTipText("Show the memory map of a TSC program");
	    viewBinFileButton.setToolTipText("View the contents of a TSC binary file");
	    binToHexButton.setToolTipText("Convert a TSC binary file to a HEX file");
		}
	}  // End of setStuff




  /* Prompt the user to save a modified document.  Save it if required. */
	private void saveModifiedDocument()
	{
	  if (docChanged)  {
			if (new YesNoDialog(this).show(null, "Save changes to " + 
	 		              tscFile.getName() + "?") == YesNoDialog.YES)
				saveItem.doClick();
		}
	}  // End of saveDocument


	
	/* The next three methods are called in response to a change in the 
	   contents of the document.  If an insert or remove is performed, 
		 the docChanged flag is set.  The third method is included to 
		 fulfill the gui's role as a document listener. */

	public void insertUpdate(DocumentEvent e)  
	{
	  if (!docChanged)  {
		  docChanged = true;
			filenameLabel.setText(tscFile.getAbsolutePath() + "  (modified)");
	  }
	}  // End of insertUpdate


	public void removeUpdate(DocumentEvent e)  
	{
	  insertUpdate(e);
	}  // End of removeUpdate


	public void changedUpdate(DocumentEvent e)  {}




	/* Upadates the line label with the current line number.  Called 
	   by the event thread every time the caret moves.  */
	public void caretUpdate(CaretEvent ce)
	{
	  int caretPos = editorArea.getCaretPosition();
		int line = 0;
	  try  {
			line = editorArea.getLineOfOffset(caretPos);
		  lineLabel.setText("Line " + (line + 1) +  "     Column " +
					(caretPos - editorArea.getLineStartOffset(line) + 1));
		} catch(BadLocationException ble)  {
		    System.out.println("Oops, error in TSCAssemblerGUI.caretUpdate(): "
				                                                + ble.getMessage());
			  System.out.println("  caretPos = " + caretPos);
			  System.out.println("  line = " + line);
		}
	}  // End of caretUpdate




	/* Respond to menu item and button events. The end of this method 
	   contains code that is executed after every action is processed.
	*/
	public void actionPerformed(ActionEvent ae)
	{
  	String ac = ae.getActionCommand();

		
		if (ac.equals("exit"))  {
		  // Save the current document if it has been modified
			saveModifiedDocument();
		  System.exit(0);
		}


		else if (ac.equals("new"))  {
		  // Save the current document if it has been modified
			saveModifiedDocument();
		
		  // Get the filename via a file dialog
      File file = fileTools.saveFile(this, "TSC", "New TSC Code File");
      
			if (file != null)  {
			  // If the file does not exist, create one so the assembler 
				// interface will accept it.
				if (!file.exists())  {
  				try  {
	  			  file.createNewFile();
					} catch (IOException e)  {
				      String message[] = new String[2];
							message[0] = "I/O Exception occurred while creating";
							message[1] = e.getMessage();
		          new OkDialog(this, true).show("File Error", message);
							return;
					}
				}
			
			  // Send the filename to the assembler interface
  			if (!tscAssembler.setCodeFile(file))  {
				  // Invalid file (this code should never need to be executed)
		      String message[] = new String[4];
					message[0] = tscFile.getAbsolutePath();
					message[1] = "could not be opened.";
					message[2] = "Make sure the file";
					message[3] = "has a .tsc extension.";
          new OkDialog(this, true).show("File Error", message);
					return;
		    }
			  
				// Valid file so clear the editor window, set the file,
				// and enable the buttons.
        editorArea.setText("");   // Clear the window
        tscFile = file;
				setStuff(true);
				docChanged = false;
			}
		}  // End of "new"



		else if (ac.equals("open"))  {
		  // Save the current document if it has been modified
			saveModifiedDocument();
		
		  // Get the filename via a file dialog
      File file = fileTools.openFile(this, "TSC", "Open a TSC Code File");
      
			if (file != null)  {
        // Send the filename to the assembler interface
  			if (!tscAssembler.setCodeFile(file))  {
				  // Invalid file (this code should never need to be executed)
		      String message[] = new String[4];
					message[0] = tscFile.getAbsolutePath();
					message[1] = "could not be opened.";
					message[2] = "Make sure the file exists";
					message[3] = "and has a .tsc extension.";
          new OkDialog(this, true).show("File Error", message);
					return;
		    }

				// Open the file stream
				RandomAccessFile tscFileStream;
				try  {
		  		tscFileStream = new RandomAccessFile(file, "r");
				} catch (IOException e)  {
			      String message[] = new String[2];
						message[0] = "I/O Exception occurred while opening";
						message[1] = e.getMessage();
	          new OkDialog(this, true).show("File Error", message);
						return;
				}

        // File is valid
				try {
			    String tempLine = null;
					
					// Read first line from file
				  tempLine = tscFileStream.readLine();
					
					/* This point is reached if the read was successful so clear
					   the editor area. 'docChanged' is temporarily set to true
						 so that the insertUpdate() method does not try to set the
						 filename label while text is being inserted into the 
						 document (tscFile may be null so the method might throw 
						 an exception).  */
					docChanged = true;
          editorArea.setText("");  // Clear the window.

					// Read in the rest of the lines from the file
				  while (tempLine != null)  {
					  editorArea.append(tempLine);
					  editorArea.append("\n");    // JTextArea doesn't like NEWLINE
				    tempLine = tscFileStream.readLine();
				  }
				} catch (IOException e)  {
			      String message[] = new String[2];
						message[0] = "I/O Exception occurred while reading";
						message[1] = e.getMessage();
	          new OkDialog(this, true).show("File Error", message);
				} finally  {
				    try  {
						  tscFileStream.close();
						} catch (IOException ioe)  {
					      String message[] = new String[2];
								message[0] = "I/O Exception occurred while closing";
								message[1] = ioe.getMessage();
			          new OkDialog(this, true).show("File Error", message);
						}
				}
				
				// Valid file so set the file and enable the buttons
        tscFile = file;
				setStuff(true);
				docChanged = false;
			}
		}  // End of "open"



		else if (ac.equals("close"))  {
		  // Save the current document if it has been modified
			saveModifiedDocument();
		
			// Clear the editor area.
      try {
				Document doc = editorArea.getDocument();
  			doc.remove(0, doc.getLength());
			} catch(BadLocationException ble)  {
			    System.out.print("TSC Assembler GUI Internal Error ");
					System.out.println("in TSCAssemblerGUI.actionPerformed(close)");
			    System.out.print("  BadLocationException in editorArea: ");
					System.out.println(ble.getMessage());
			}
			
			// Change the focus to something other than the editor window 
			// and reset the buttons.
			setStuff(false);
			outputArea.requestFocus();
			docChanged = false;
		}  // End of "close"
    
		
		
		else if (ac.equals("save"))  {
			RandomAccessFile tscFileStream;
	
	    // Delete an existing .tsc file
      tscFile.delete();

      // Open the file stream
			try  {
	  		tscFileStream = new RandomAccessFile(tscFile, "rw");
			} catch (IOException e)  {
		      String message[] = new String[2];
					message[0] = "I/O Exception occurred while saving";
					message[1] = e.getMessage();
          new OkDialog(this, true).show("File Error", message);
					return;
			}

      // Write the data to the file
			try  {
			  tscFileStream.writeBytes(editorArea.getText());
			} catch (IOException e)  {
		      String message[] = new String[2];
					message[0] = "I/O Exception occurred while writing";
					message[1] = e.getMessage();
          new OkDialog(this, true).show("File Error", message);
					return;
			} finally  {
			    try  {
					  tscFileStream.close();
					} catch (IOException ioe)  {
				      String message[] = new String[2];
							message[0] = "I/O Exception occurred while closing";
							message[1] = ioe.getMessage();
		          new OkDialog(this, true).show("File Error", message);
							return;
					}
			}

			// Remove the '*' from the filename label
			filenameLabel.setText(tscFile.getAbsolutePath());
			docChanged = false;

    }  // End of "save"



		else if (ac.equals("save as"))  {
		  // Get the filename via a file dialog
      File file = fileTools.saveFile(this, "TSC", "Save TSC Code File As...");
      
			if (file != null)  {
			  // If the file does not exist, create one so the assembler 
				// interface will accept it.
				if (!file.exists())  {
  				try  {
	  			  file.createNewFile();
					} catch (IOException e)  {
				      String message[] = new String[2];
							message[0] = "I/O Exception occurred while creating";
							message[1] = e.getMessage();
		          new OkDialog(this, true).show("File Error", message);
							return;
					}
				}
			
			  // Send the filename to the assembler interface
  			if (!tscAssembler.setCodeFile(file))  {
				  // Invalid file (this code should never need to be executed)
		      String message[] = new String[4];
					message[0] = tscFile.getAbsolutePath();
					message[1] = "could not be created.";
					message[2] = "Make sure the file";
					message[3] = "has a .tsc extension.";
          new OkDialog(this, true).show("File Error", message);
					return;
		    }
				tscFile = file;
				saveItem.doClick();
				setStuff(true);    // Update the label and tooltips
				docChanged = false;
			}
		}  // End of "save as"



		else if (ac.equals("cut"))  {
			  editorArea.cut();
		}  // End of "cut"



		else if (ac.equals("copy"))  {
  		  editorArea.copy();
		}  // End of "copy"



		else if (ac.equals("paste"))  {
  		  editorArea.paste();
		}  // End of "paste"



		else if (ac.equals("word wrap"))  {
		  editorArea.setLineWrap(wordWrapItem.isSelected());
		}  // End of "word wrap"



		else if (ac.equals("assembler"))  {
		  // Save the file
			if (saveBeforeAssembleItem.isSelected())
				saveItem.doClick();
		  // Set the option flags
		  tscAssembler.showSymbolTable = showSymTableItem.isSelected();
		  tscAssembler.showSegmentInfo = showSegmentInfoItem.isSelected();
      // Run the assembler
      tscAssembler.go();
      SwingUtilities.invokeLater(ensureOutputIsVisible);
		}  // End of "assemble"



		else if (ac.equals("memory map viewer"))  {
		  String filename = FileTools.replaceExtension(tscFile.getName(), "bin");
      System.out.println(NEWLINE + "The Memory Map of " + filename + ":");
			TSCMemoryMap.showMemMap(new File(tscFile.getParent() + "/" + filename));
      SwingUtilities.invokeLater(ensureOutputIsVisible);
		}  // End of "view memory map"



    else if (ac.equals("binary file viewer"))  {
		  String filename = FileTools.replaceExtension(tscFile.getName(), "bin");
      System.out.println(NEWLINE + "The Contents of " + filename + " in hex:");
			TSCBinFileViewer.viewFile(new File(tscFile.getParent() + "/" + filename));
      SwingUtilities.invokeLater(ensureOutputIsVisible);
    }  // End of "view binary file"



    else if (ac.equals("bin to hex"))  {
		  String filename = FileTools.replaceExtension(tscFile.getName(), "bin");
      System.out.println(NEWLINE + "Converting " + filename + " to " +
			           FileTools.replaceExtension(filename, "hex") + " ...");
			if (TSCBinFileToHexFile.convert(
			    new File(tscFile.getParent() + "/" + filename)))  {
			  System.out.println("Conversion completed.");
			}
      SwingUtilities.invokeLater(ensureOutputIsVisible);
    }  // End of "bin to hex"




		else if (ac.equals("clear output"))  {
      outputArea.setText("");
		}  // End of "clear output"



		
		else if (ac.equals("inc tab size"))  {
      if (tabSize < MAX_TAB_SIZE)  {
			
			  if (tabSize == MIN_TAB_SIZE)
				  decTabSizeItem.setEnabled(true);
				tabSize++;
				decTabSizeItem.setText("Decrease Tab Size to " + (tabSize - 1));
			  if (tabSize != MAX_TAB_SIZE)
				  incTabSizeItem.setText("Increase Tab Size to " + (tabSize + 1));
				else
				  incTabSizeItem.setEnabled(false);
			}
		}  // End of "inc tab size"

		


		else if (ac.equals("dec tab size"))  {
      if (tabSize > MIN_TAB_SIZE)  {
			
			  if (tabSize == MAX_TAB_SIZE)
				  incTabSizeItem.setEnabled(true);
				tabSize--;
				incTabSizeItem.setText("Increase Tab Size to " + (tabSize + 1));
			  if (tabSize != MIN_TAB_SIZE)
  				decTabSizeItem.setText("Decrease Tab Size to " + (tabSize - 1));
				else
				  decTabSizeItem.setEnabled(false);
			}
		}  // End of "dec tab size"

		
		
		// Ocasionally, the menus do not disappear after an item has been 
		// chosen so repaint to remove any menus.
		repaint();
		
		// If the editor window accepts input, request the focus for it 
		// after every button or menu action
		if (editorArea.isEnabled())
		  editorArea.requestFocus();

	}  // End of actionPerformed




  /* This class ensures that any output just printed to the 
	   output area is viewable in the window i.e. the scroll 
		 pane must scroll down to the bottom of the text.  This 
		 class is inserted into the swing event thread so that 
		 it gets the most current text area dimensions.  */
  private class EnsrVis implements Runnable
	{
		public void run()
		{
		  JViewport viewport = outputScrollPane.getViewport();
			Dimension es = viewport.getExtentSize();
			Dimension ts = outputArea.getSize();
			viewport.setViewPosition(new Point(0, ts.height - es.height));
	  }  // End of run
  }  // End of EnsureVisible class
	
	
	
	
	/* This class provides a fixed size invisible separator 
	   to put between menu headings on the menu bar.  */
	private class MnuPad extends JSeparator
	{
	  public MnuPad()
		{
		  super(SwingConstants.VERTICAL);
			setMaximumSize(new Dimension(25, 21));
			setMinimumSize(new Dimension(25, 21));
			setPreferredSize(new Dimension(25, 21));
			setVisible(false);
		}  // End of constructor
	}  // End of MnuPad class




	/* This class is used to redirect standard output and standard 
	   error to the output area of the assembler GUI.  */
	private class TxtOutput extends OutputStream
	{

    /* Appends a character to the output area in the assembler GUI. */
		public void write(int b) throws IOException
		{
		  outputArea.append(String.valueOf((char) b));
		}  // End of write



		/* Appends a string to the output area in the assembler GUI. */
    public void write(byte[] b) throws IOException
		{
      outputArea.append(new String(b));
		}  // End of write



		/* Appends part of a string to the output area in the assembler 
		   GUI.  I'm pretty sure this is the method that println calls.  
			 When this method is removed, large outputs take several 
			 minutes to appear in the text area.  When this method is 
			 added, large outputs took a couple seconds to appear.  */
    public void write(byte b[], int off, int len) throws IOException
		{
      outputArea.append(new String(b, off, len));
    }  // End of write(,,)

	}  // End of TxtOutput class




  /* This class handles the tab event in the editor window.  If text 
	   is not selected, a tab string is inserted at the caret position.
		 If text is selected and tab is pressed, each line in the 
		 selection is indented one tab string.  If text is selected and 
		 shift-tab is pressed, each line is outdented one tab string.  */
	private class TabAction extends AbstractAction
	{

    public void actionPerformed(ActionEvent ae)
		{
		  if (editorArea.isEnabled())  {
				
				// If no text is selected, insert a tab
			  if (editorArea.getSelectionStart() == editorArea.getSelectionEnd())  {
				  int caretPosition = editorArea.getCaretPosition();
          try  {
						// Nice code, eh?
	  		    editorArea.insert(tabString[tabSize - ((caretPosition - 
						    editorArea.getLineStartOffset(editorArea.getLineOfOffset(
								caretPosition))) % tabSize)], caretPosition);
	        } catch(BadLocationException ble) {
					    System.out.print("TSC Assembler GUI Internal Error ");
	  					System.out.println("in TSCAssemblerGUI.TabAction");
					    System.out.print("  BadLocationException in editorArea: ");
							System.out.println(ble.getMessage());
					}
				}

        // Text is selected.
        else try  {
	        int startLine = editorArea.getLineOfOffset(
					            editorArea.getSelectionStart());
	        int endLine = editorArea.getLineOfOffset(
					        editorArea.getSelectionEnd() - 1);
          
					// If the highlight ends in the middle of a line, extend 
					// the highlight to the end of that line.
				  editorArea.setSelectionEnd(editorArea.getLineEndOffset(endLine));
					
					if ((ae.getModifiers() & Event.SHIFT_MASK) == Event.SHIFT_MASK)  {
						
						// Shift-tab was pressed
						String textLine = null;
						int lineStartOffset;
						int lineEndOffset;
						int lineTabSize;
            int numSpaces;

						// Outdent each line depending on the number of spaces at 
						// the beginning of the line.
						for (int line = startLine; line <= endLine; line++)  {
							lineStartOffset = editorArea.getLineStartOffset(line);
						  lineEndOffset = editorArea.getLineEndOffset(line) - 1;
							
							// If the line is empty, go to the next line.
							if (lineEndOffset != lineStartOffset)  {

							  // Find the tab size for each line.  The line tab 
								// size is the current tab size OR if the line is 
								// smaller than that, it is the length of the line.
							  lineTabSize = tabSize;
								if (lineEndOffset - lineStartOffset < lineTabSize - 1)
								  lineTabSize = lineEndOffset - lineStartOffset + 1;

                // Find the number of spaces at the beginning of the line.
								// If the length of the tab string is greater than the
								// number of starting spaces, only outdent the number of
								// starting spaces.
                textLine = editorArea.getText(lineStartOffset, lineTabSize);
							  for (numSpaces = 0; numSpaces < lineTabSize; numSpaces++)
									if (textLine.charAt(numSpaces) != ' ')  break;

								// Perform the outdenting.
								if (numSpaces > 0)
									editorArea.replaceRange(null, lineStartOffset, 
									                  lineStartOffset + numSpaces);
							}
						}
					}

          else  {
						// The tab was pressed so indent every line.
						for (int line = startLine; line <= endLine; line++)
						  editorArea.insert(tabString[tabSize],
							    editorArea.getLineStartOffset(line));
					}

					// If the highlight starts in the middle of a line, extend the 
					// highlight to the start of that line.  (For some reason, this 
					// only works AFTER the tabs are insertsed.)
				  editorArea.setSelectionStart(editorArea.getLineStartOffset(startLine));

        } catch(BadLocationException ble) {
				    System.out.print("TSC Assembler GUI Internal Error ");
  					System.out.println("in TSCAssemblerGUI.TabAction");
				    System.out.print("  BadLocationException in editorArea: ");
						System.out.println(ble.getMessage());
				}
			}
		}  // End of actionPerformed
		
	}  // End of TabAction class

}  // End of TSCAssemblerGUI class

