/*
© 2000, Performance Evaluation Laboratory, Brigham Young University. Reproduction of all or part of this work is permitted for non-profit educational
or research use provided this copyright notice remains intact. All other rights reserved. 
*/

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;

class IOConsole extends JPanel implements ActionListener
{
	final static int FILE_ERROR = Constants.FILE_ERROR;
  final static Insets ZERO_INSET = Constants.ZERO_INSET;
  final static ImageIcon yellowLightOnImage = Constants.yellowLightOnImage;
  final static ImageIcon yellowLightOffImage = Constants.yellowLightOffImage;
  final static ImageIcon greenLightOnImage = Constants.greenLightOnImage;
  final static ImageIcon greenLightOffImage = Constants.greenLightOffImage;
  final static ImageIcon redLightOnImage = Constants.redLightOnImage;
  final static ImageIcon redLightOffImage = Constants.redLightOffImage;
  private static String errorTitle = new String("I/O CONSOLE: I/O ERROR");
  private static String errorString[] = new String[3];

	private ImageIcon wallpaper;
	private int wallpaperWidth;
	private int wallpaperHeight;

  private ButtonBugFix iconBugFix = new ButtonBugFix();

  private Display display;
	private Monitor monitor;

	private File inputFile = null;
	private File outputFile = null;
  private RandomAccessFile inStream = null;
	private RandomAccessFile outStream = null;

	private JRadioButton lights[] = new JRadioButton[16];
	private JLabel sendDataLight;
	private JLabel ackDataLight;
  private JButton sendDataButton;
	private JButton ackDataButton;
	public JCheckBox autoAckCheckBox;
	public JCheckBox inputConsoleCheckBox;
	private JCheckBox inputFileCheckBox;
  public JCheckBox outputConsoleCheckBox;
	private JCheckBox outputFileCheckBox;
	private GridBagLayout gridBag;
	private GridBagConstraints gbc;



	
  public IOConsole(Display display)
	{
	  this.display = display;
		this.monitor = display.monitor;

    setBackground(Constants.BORING_GRAY_2);
    setBorder(BorderFactory.createRaisedBevelBorder());
		gridBag = new GridBagLayout();  // gridbag used for all components
    setLayout(gridBag);
    gbc = new GridBagConstraints();  // gbc is used for all components
		gbc.weightx = 0;  gbc.weighty = 0;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = ZERO_INSET;
    gridBag.setConstraints(this, gbc);
		
    // IO Label
		JLabel ioLabel = new JLabel("I/O CONSOLE", JLabel.CENTER);
		ioLabel.setForeground(Constants.BLACK);
    Font oldFont = ioLabel.getFont();
		ioLabel.setFont(new Font(oldFont.getName(), oldFont.getStyle(), 20));
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.insets = new Insets(10, 0, 8, 0);
		add(ioLabel, gbc);

    // Create "lights" panel
    JPanel lightsPanel = new JPanel(gridBag);
		lightsPanel.setOpaque(false);
		lightsPanel.setToolTipText("I/O Console Lights");
		gbc.insets = new Insets(0, 15, 0, 15);
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

		// Create Light labels (add to light panel)
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

		// Create Send Data button with image label
		sendDataLight = new JLabel(yellowLightOffImage, JLabel.RIGHT);
		sendDataLight.setToolTipText(
		  "Turns yellow when data is requested via the RWD instruction");
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0, 40, 0, 0);
		gbc.gridwidth = 1;
		gbc.gridx = GridBagConstraints.RELATIVE;
		gbc.gridy = GridBagConstraints.RELATIVE;
		add(sendDataLight, gbc);

		sendDataButton = new JButton(new ImageIcon("images/button-senddata.gif"));
    sendDataButton.setPressedIcon(new ImageIcon("images/button-senddata-pressed.gif"));
		sendDataButton.setOpaque(false);
    sendDataButton.setBorderPainted(false);
    sendDataButton.setContentAreaFilled(false);
		sendDataButton.setFocusPainted(false);
		sendDataButton.setMargin(ZERO_INSET);
		sendDataButton.setEnabled(false);
		sendDataButton.setActionCommand("send data");
		sendDataButton.addActionListener(this);
		sendDataButton.setToolTipText(
		  "Send the numeric value represented by the Console Lights " +
			"to the input port of the computer");
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.insets = new Insets(10, 10, 10, 0);
    add(sendDataButton, gbc);		

    // Create Acknowledge Data button with image label
		ackDataLight = new JLabel(yellowLightOffImage, JLabel.RIGHT);
		ackDataLight.setToolTipText(
		  "Turns yellow when a data acknowledgement is requested " +
			"via the WWD instruction");
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0, 40, 0, 0);
		add(ackDataLight, gbc);

		ackDataButton = new JButton(new ImageIcon("images/button-ackdata.gif"));
    ackDataButton.setPressedIcon(new ImageIcon("images/button-ackdata-pressed.gif"));
		ackDataButton.setOpaque(false);
    ackDataButton.setBorderPainted(false);
    ackDataButton.setContentAreaFilled(false);
		ackDataButton.setFocusPainted(false);
		ackDataButton.setMargin(ZERO_INSET);
		ackDataButton.setEnabled(false);
		ackDataButton.setActionCommand("ack data");
		ackDataButton.addActionListener(this);
		ackDataButton.setToolTipText(
		  "Send an acknowledgement to the computer verifying that the " +
			"output data was displayed on the Console Lights");
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(10, 10, 10, 50);
    add(ackDataButton, gbc);

    // Create Auto-acknowledge checkbox
		autoAckCheckBox = new JCheckBox("Auto-acknowledge", redLightOffImage);
    autoAckCheckBox.setSelectedIcon(redLightOnImage);
    autoAckCheckBox.setDisabledIcon(redLightOffImage);
		autoAckCheckBox.setOpaque(false);
		autoAckCheckBox.addItemListener(iconBugFix);
		autoAckCheckBox.setFocusPainted(false);
		autoAckCheckBox.setToolTipText(
		  "Automatically send a data acknowledgement to the computer " +
			"eliminating the need to press the Acknowledge Data button");
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0, 0, 0, 60);
    add(autoAckCheckBox, gbc);
		
    // Create Input Source and Output Dest labels
    JLabel inputSourceLabel = new JLabel("Input Source", JLabel.CENTER);
		inputSourceLabel.setForeground(Constants.BLACK);
		gbc.gridwidth = 2;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.insets = new Insets(10, 40, 5, 0);
		add(inputSourceLabel, gbc);
    JLabel outputDestLabel = new JLabel("Output Destination", JLabel.CENTER);
		outputDestLabel.setForeground(Constants.BLACK);
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.insets = new Insets(10, 30, 5, 10);
		add(outputDestLabel, gbc);

    // Create Source/Dest check box panel
		JPanel sourceDestPanel = new JPanel(gridBag);
		sourceDestPanel.setOpaque(false);
		gbc.insets = new Insets(0, 0, 10, 0);
    gridBag.setConstraints(sourceDestPanel, gbc);

		// Create Input Source check boxes and add to S/D Panel
		inputConsoleCheckBox = new JCheckBox("Console", redLightOffImage, true);
    inputConsoleCheckBox.setSelectedIcon(redLightOnImage);
    inputConsoleCheckBox.setDisabledIcon(redLightOnImage);
		inputConsoleCheckBox.addItemListener(iconBugFix);
		inputConsoleCheckBox.setOpaque(false);
		inputConsoleCheckBox.setFocusPainted(false);
		inputConsoleCheckBox.setToolTipText(
		  "Cause the input data to be taken from the Console Lights");
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0, 10, 0, 0);
    sourceDestPanel.add(inputConsoleCheckBox, gbc);

		inputFileCheckBox = new JCheckBox("File", redLightOffImage, false);
    inputFileCheckBox.setSelectedIcon(redLightOnImage);
    inputFileCheckBox.setDisabledIcon(redLightOffImage);
		inputFileCheckBox.setOpaque(false);
		inputFileCheckBox.addItemListener(iconBugFix);
		inputFileCheckBox.setFocusPainted(false);
		inputFileCheckBox.setEnabled(false);
		inputFileCheckBox.setToolTipText(
		  "Cause the input data to be taken from the Input File");
		gbc.insets = new Insets(0, 20, 0, 35);
    sourceDestPanel.add(inputFileCheckBox, gbc);
		
		// Create Input button group
		ButtonGroup inputGroup = new ButtonGroup();
		inputGroup.add(inputConsoleCheckBox);
		inputGroup.add(inputFileCheckBox);

		/* Create Output Dest check boxes and add to S/D Panel
		   Both boxes can be checked at the same time so there is
		   no 'output button group'.  The action listener must 
			 ensure that at least one box is selected at all times. */
		outputConsoleCheckBox = new JCheckBox("Console", redLightOffImage, true);
    outputConsoleCheckBox.setSelectedIcon(redLightOnImage);
    outputConsoleCheckBox.setDisabledIcon(redLightOnImage);
		outputConsoleCheckBox.setOpaque(false);
		outputConsoleCheckBox.addItemListener(iconBugFix);
		outputConsoleCheckBox.setFocusPainted(false);
		outputConsoleCheckBox.setActionCommand("output dest-console");
		outputConsoleCheckBox.addActionListener(this);
		outputConsoleCheckBox.setToolTipText(
		  "Cause the output data to be displayed on the Console Lights");
		gbc.insets = new Insets(0, 45, 0, 0);
    sourceDestPanel.add(outputConsoleCheckBox, gbc);

		outputFileCheckBox = new JCheckBox("File", redLightOffImage, false);
    outputFileCheckBox.setSelectedIcon(redLightOnImage);
    outputFileCheckBox.setDisabledIcon(redLightOffImage);
		outputFileCheckBox.setOpaque(false);
		outputFileCheckBox.addItemListener(iconBugFix);
		outputFileCheckBox.setFocusPainted(false);
		outputFileCheckBox.setEnabled(false);
		outputFileCheckBox.setActionCommand("output dest-file");
		outputFileCheckBox.addActionListener(this);
		outputFileCheckBox.setToolTipText(
		  "Cause the output data to be written to the Output File");
		gbc.insets = new Insets(0, 20, 0, 0);
    sourceDestPanel.add(outputFileCheckBox, gbc);
		
		add(sourceDestPanel);

	}  // End of constructor




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
		 executing.  Called when 'start' or 'step' is pressed. 
     The send and ack buttons are always disabled except 
		 when the Sim requests I/O. */
  public void setEnabledButtons(boolean enable)
	{
    autoAckCheckBox.setEnabled(enable);
    inputConsoleCheckBox.setEnabled(enable);
		if (inStream == null)
  	  inputFileCheckBox.setEnabled(false);
		else
  	  inputFileCheckBox.setEnabled(enable);
	  outputConsoleCheckBox.setEnabled(enable);
		if (outStream == null)
  	  outputFileCheckBox.setEnabled(false);
		else
  	  outputFileCheckBox.setEnabled(enable);
    setEnabledSendButton(false);
    setEnabledAckButton(false);
	} // End of setEnabledButtons




  // Returns the value represented by the lights
	private int getLightsValue()
	{
	  int value = 0;
		
    for (int i = 0; i <16; i++)
      if (lights[i].isSelected())
		    value = value | (1 << i);

    return value;
	}  // End of getLightsValue




  /* Sets the binary representation of 'value' 
	   into the lights. */
	public void setLights(int value)
	{
    for (int i = 0; i <16; i++)  {
      if ((value & 0x00000001) == 1)
			  lights[i].setSelected(true);
			else
			  lights[i].setSelected(false);
	    value = value >> 1;
		}
	}  // End of setLights
	



    /* Enables (disables) the 'send data' button.  The button is only 
	   enabled when the Sim requests input data.  If stop is pressed 
		 before the 'send data' button, the 'send data' is disabled and 
		 Sim is informed that stop has been pressed. */
	private void setEnabledSendButton(boolean enable)
	{
	  sendDataButton.setEnabled(enable);
    if (enable)
  		sendDataLight.setIcon(yellowLightOnImage);
		else
      sendDataLight.setIcon(yellowLightOffImage);
	}  // End of setEnabledSendButton




  /* Enables (disables) the 'ack data' button.  The button is only 
	   enabled when the Sim sends output data.  If stop is pressed 
		 before the 'ack data' button, the 'ack data' is disabled and 
		 Sim is informed that the ack has been cancelled. */
	private void setEnabledAckButton(boolean enable)
	{
	  ackDataButton.setEnabled(enable);
    if (enable)
      ackDataLight.setIcon(yellowLightOnImage);
		else
  	  ackDataLight.setIcon(yellowLightOffImage);
	}  // End of setEnabledAckButton




  /* Sets up the input file stream.  Returns true if the 
	   input data stream could be opened.  If the file == null,
	   the input file checkbox is disabled. */
	public boolean setInputFile(File inputFile)
	{
  	if (inStream != null)  {  // Close current file
		  try  {
			  inStream.close();
  		}	 catch (IOException e)  {
					 errorString[0] = "Cannot close";
					 errorString[1] = inputFile.getPath();
 					 errorString[2] = e.getMessage();
		       new OkDialog(display, true).show(errorTitle, errorString);
  		}
		}
		
		if (inputFile != null)  {  // Create new data stream to read shorts
		  try {
  	    inStream = new RandomAccessFile(inputFile, "r");
  		}	 catch (IOException e)  {
					 errorString[0] = "Cannot open";
					 errorString[1] = inputFile.getPath();
 					 errorString[2] = e.getMessage();
		       new OkDialog(display, true).show(errorTitle, errorString);

				   closeInputFile();
			     return false;
  		}
      this.inputFile = inputFile;
  		inputFileCheckBox.setEnabled(true);
			inputFileCheckBox.doClick();
			return true;
		}
    else  {   // inputFile == null
    	inputFileCheckBox.setEnabled(false);
	    inputConsoleCheckBox.doClick();
      this.inputFile = null;
			return false;
		}
	}  // End of setInputFile




  /* Sets up the output file stream.  Returns true if the 
	   output data stream was opened.  If file == null
	   the output file checkbox is disabled.  */
	public boolean setOutputFile(File outputFile)
	{
		if (outStream != null)   // Close current file
		  try  {
			  outStream.close();
  		}	 catch (IOException e)  {
					 errorString[0] = "Cannot close";
					 errorString[1] = outputFile.getPath();
 					 errorString[2] = e.getMessage();
		       new OkDialog(display, true).show(errorTitle, errorString);
  		}

		if (outputFile != null)  {
      // Create new data stream to write shorts
		  try {
  	    outStream = new RandomAccessFile(outputFile, "rw");
  		}	 catch (IOException e)  {
					 errorString[0] = "Cannot write to";
					 errorString[1] = outputFile.getPath();
 					 errorString[2] = null;
		       new OkDialog(display, true).show(errorTitle, errorString);

  				 closeOutputFile();
			     return false;
  		}
      this.outputFile = outputFile;
  		outputFileCheckBox.setEnabled(true);
			return true;
		}
		else  {  // outputFile == null
   	  outputFileCheckBox.setEnabled(false);
      outputConsoleCheckBox.doClick();
      this.outputFile = null;
			return false;
		}
	}  // End of setInputFile




	/* Reset the input file. */
	public void resetInputFile()
	{
    if (inputFile != null) 
	    try  {
	  		inStream.seek(0);
			}  catch (IOException e)  {
					 errorString[0] = "Error in resetting";
					 errorString[1] = inputFile.getPath();
 					 errorString[2] = e.getMessage();
		       new OkDialog(display, true).show(errorTitle, errorString);
			}
	}  // End of resetFiles




	/* Reset the output file. */
	public void resetOutputFile()
	{
    if (outputFile != null)  {
	    try  {
	  		outStream.seek(0);
			} catch (IOException e)  {
					errorString[0] = "Error in resetting";
					errorString[1] = outputFile.getPath();
 				  errorString[2] = e.getMessage();
		      new OkDialog(display, true).show(errorTitle, errorString);
			}
    }
	}  // End of resetFiles




  /* Close the input file. */
	public void closeInputFile()
	{
    if (inStream != null)  {
	    try  {
	  		inStream.close();
			} catch (IOException e)  {
				  errorString[0] = "Error in closing";
				  errorString[1] = inputFile.getPath();
				  errorString[2] = e.getMessage();
	   	    new OkDialog(display, true).show(errorTitle, errorString);
			}
			inputFile = null;
			inStream = null;
			display.updateInputFileMenu(null);
			inputFileCheckBox.setEnabled(false);
	    inputConsoleCheckBox.doClick();
		}
	}  // End of closeInputfile
	

	
	
  /* Close the output file. */
	public void closeOutputFile()
	{
    if (outStream != null)  {
	    try  {
	  		outStream.close();
			} catch (IOException e)  {
				  errorString[0] = "Error in closing";
				  errorString[1] = outputFile.getPath();
				  errorString[2] = e.getMessage();
	   	    new OkDialog(display, true).show(errorTitle, errorString);
			}
			outputFile = null;
			outStream = null;
			display.updateOutputFileMenu(null);
   	  outputFileCheckBox.setEnabled(false);
      outputConsoleCheckBox.doClick();
		}
	}  // End of closeInputfile
	

	
	
  /* This method either enables the 'send' button so that the 
	   'light data' can be sent to the monitor (the actual
		 transmission of the data is done by the actionPerformed()
		 method) or it reads two bytes of data from a file and sends 
		 it to the monitor.  */
  public void sendData()
	{	
    if (inputConsoleCheckBox.isSelected())
		  setEnabledSendButton(true);

		else  {    // if the console is NOT selected then a file IS selected
  		try {
  		  monitor.sendInputData(inStream.readShort() & 0x0000ffff);
		  } catch (EOFException e)  {  // End of File
				  errorString[0] = "End of input file";
				  errorString[1] = "Resetting  " + inputFile.getPath();
				  errorString[2] = null;
	   	    new OkDialog(display, true).show(errorTitle, errorString);

	        resetInputFile();        // Go back to the beginning of the file
	  			monitor.executionHalted(false);              // Stop the Sim
					monitor.cancelInputData();     // Cancel and wake up the Sim
  		} catch (IOException e)  {
				  errorString[0] = "Error in reading";
				  errorString[1] = inputFile.getPath();
				  errorString[2] = e.getMessage();
	   	    new OkDialog(display, true).show(errorTitle, errorString);

  			  monitor.executionHalted(false);              // Stop the Sim
				  monitor.cancelInputData();  // Cancel data and wake up the Sim
  		}
		}
  }  // End of sendData




  /* This method either enables the 'ack' button so that the 
	   'light data' can be acked (the actual acking of the data is 
		 done by the actionPerformed() method) or it writes the 'light
		 data' to a file and sends the ack to the monitor.  
		
		 If both the console and the file are the destination (this is 
		 possible for output data but not for the input data) the 'ack' 
		 button is enabled and the actionPerformed() method does the 
		 acking and the writing to the file.  If auto ack is enabled, 
		 the ackDataButton is artificially pressed here.  Auto ack has 
		 no effect if the console is not one of the destinations. */

  public void receiveData(int data)
	{
    if (outputConsoleCheckBox.isSelected())  {
		  setLights(data);
  	  setEnabledAckButton(true);
      if (autoAckCheckBox.isSelected())
			  ackDataButton.doClick();       // Push the ack button
		}
    else  {    // Console is not selected so write to the file
      if (writeToOutputFile(data))
				monitor.ackOutputData();    // Send the ack
		}
  }  // End of receiveData
	



  /* Write the output data to the output file. Delete the output 
	   file if the file offset is 0 so that old data is erased. */
  private boolean writeToOutputFile(int data)
	{
		try {  // If at the start of the output file, erase the old data
		  if (outStream.getFilePointer() == 0)  {
			  outStream.close();
				outStream = null;
			  outputFile.delete();
				setOutputFile(outputFile);
    		outputFileCheckBox.setEnabled(false); //Because the Sim is executing
			}
		} catch (IOException e)  {
			  errorString[0] = "Error in clearing";
			  errorString[1] = outputFile.getPath();
			  errorString[2] = e.getMessage();
   	    new OkDialog(display, true).show(errorTitle, errorString);

  			monitor.executionHalted(false);              // Stop the Sim
		  	monitor.cancelOutputAck();    // Cancel ack and wake up the Sim
			  return false;
		}

		try {
		  outStream.writeShort(data);
		} catch (IOException e)  {
			  errorString[0] = "Error in writing to";
			  errorString[1] = outputFile.getPath();
			  errorString[2] = e.getMessage();
   	    new OkDialog(display, true).show(errorTitle, errorString);

				monitor.executionHalted(false);              // Stop the Sim
				monitor.cancelOutputAck();    // Cancel ack and wake up the Sim
				return false;
		}
  	return true;
	}  // End of writeOutputToFile




  // Responds to all button and checkbox events in the IO Console
  public void actionPerformed(ActionEvent e)
	{
	  String ac = e.getActionCommand();
		
    if (ac.equals("send data"))  {
			setEnabledSendButton(false);
      monitor.sendInputData(getLightsValue());
		}

    // If you're here, the console is one of the destinations
		else if (ac.equals("ack data"))  {
			setEnabledAckButton(false);
	    if (outputFileCheckBox.isSelected())  {
			  // DO NOT ack console data if the file write fails
        if (writeToOutputFile(getLightsValue()))
          monitor.ackOutputData();
			}
			else           // File is not a dest. so just ack data
			  monitor.ackOutputData();
    }

    else if (ac.equals("output dest-console"))  {
		  // Ensure that at least 1 dest. is selected at all times
      if (!outputFileCheckBox.isEnabled())  {
			  outputFileCheckBox.setSelected(false);
			  outputConsoleCheckBox.setSelected(true);
			}
      else if (!outputConsoleCheckBox.isSelected())
			  outputFileCheckBox.setSelected(true);
		}

    else if (ac.equals("output dest-file"))  {
		  // Ensure that at least 1 dest. is selected at all times
      if (!outputFileCheckBox.isSelected())
			  outputConsoleCheckBox.setSelected(true);
    }  
  }   // End of actionPerformed

}  // End of IOConsole class

