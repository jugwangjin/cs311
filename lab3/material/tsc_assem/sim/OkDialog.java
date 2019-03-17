/*
© 2000, Performance Evaluation Laboratory, Brigham Young University. Reproduction of all or part of this work is permitted for non-profit educational
or research use provided this copyright notice remains intact. All other rights reserved. 
*/

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;


/*  This class provides a general dialog that displays a message 
    and an OK button.  The dialog is shown by calling 'show'.  */

class OkDialog extends Dialog implements ActionListener
{

	private Dimension parentDim;  // The parent frame's dimensions
	private Point parentLoc;   // The parent frame's location on the screen



  public OkDialog(Frame frame, boolean modal)
	{
	  super(frame, modal);
		setLayout(new GridBagLayout());
		
    // Get the parent frames dimensions
		parentDim = frame.getSize();
		parentLoc = frame.getLocationOnScreen();

		// Respond to WindowClosing event
		addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e)  {
        dispose();
			}
	  });
		
	}  // End of constructor




/*  The dialog is shown when this method is called.  'title' is 
    displayed in the dialog title bar and 'message' is displayed 
		above the buttons.  'message' may be a string or an array 
		of strings.  If it is anything else, it's toString() method 
		is called.  Null strings within string arrays are ignored.  */

  public void show(String title, Object message)
	{
	  GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.insets = new Insets(20, 20, 0, 20);
		
    if (title != null)  setTitle(title);	
	  if (message == null)  message = " ";

    // If its a string, add it to the dialog
    if (message instanceof String)  {
		  JLabel label = new JLabel((String) message);
			label.setForeground(Color.black);
			add(label, gbc);
    }

    // If it's a string array, add it to the dialog one line at a time.
		// (nulls are ignored)
		else if (message instanceof String[])  {
		  String mes[] = (String[]) message;
			if (mes.length == 0)                   // The array is empty
   			add(new JLabel(new String(" ")), gbc);  // Add a blank line
			else  {
    		gbc.insets = new Insets(0, 20, 2, 20);
   			add(new JLabel(new String(" ")), gbc);  // Add padding
			  for (int i = 0; i < mes.length; i++)  {
				  if (mes[i] != null)  {
					  if (mes[i] == "")
		    			add(new JLabel(new String(" ")), gbc); // Add a blank line
						else  {
						  JLabel label = new JLabel(mes[i]);
							label.setForeground(Color.black);
							add(label, gbc);
						}
					}
				}
			}
		}

    // It is not a string so it's toString() method is called
		else  {
		  JLabel label = new JLabel(message.toString());
			label.setForeground(Color.black);
		  add(label, gbc);
		}

    // Add the OK button
    Button okButton = new Button("OK");
		okButton.addActionListener(this);
		okButton.setActionCommand("ok");
		gbc.insets = new Insets(13, 20, 15, 20);
		add(okButton, gbc);

		pack();

    // Center the dialog in the middle of the parent frame
    Dimension dialogDim = getSize();
		setLocation(parentLoc.x + ((parentDim.width - dialogDim.width) / 2), 
  		          parentLoc.y + ((parentDim.height - dialogDim.height) / 2));

		super.show();

    // If the dialog is modal then execution will stall here 
		// until a button is pressed or the dialog is closed.
	
	}  // End of show
	
	
	
	
	public void actionPerformed(ActionEvent ae)
	{
		if (ae.getActionCommand().equals("ok"))  {
			dispose();
		}
	}  // End of actionPerformed

}  // End of OkDialog class


