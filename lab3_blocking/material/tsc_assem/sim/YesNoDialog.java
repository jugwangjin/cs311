/*
© 2000, Performance Evaluation Laboratory, Brigham Young University. Reproduction of all or part of this work is permitted for non-profit educational
or research use provided this copyright notice remains intact. All other rights reserved. 
*/

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;


/*  This class provides a general modal dialog that displays yes and 
    no buttons.  The dialog is shown by calling 'show'.  The response
		YES, NO, or CLOSE is returned by the 'show' method depending on 
		the user action. */

class YesNoDialog extends Dialog implements ActionListener
{
  final static int YES = 1;
  final static int NO = 2;
  final static int CLOSE = 3;    // Window closed with no buttons pressed

  private int response = CLOSE;
	private Dimension parentDim;   // The parent frames dimensions
	private Point parentLoc;   // The parent frame's location on the screen




  public YesNoDialog(Frame frame)
	{
	  super(frame, true);
		setLayout(new GridBagLayout());
		
    // Get the parent frames dimensions
		parentDim = frame.getSize();
		parentLoc = frame.getLocationOnScreen();

		// Respond to WindowClosing event
		addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        dispose();
      }
	  });
		
	}  // End of constructor




/*  The dialog is shown when this method is called.  'title' is 
    displayed in the dialog title bar and 'message' is displayed 
		above the buttons.  'message' may be a string or an array 
		of strings.  If it is anything else, it's toString() method 
		is called.  Null strings within string arrays are ignored. 
		The response YES, NO, or CLOSE is returned by the 'show' 
		method depending on the user action. */

  public int show(String title, Object message)
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

    // If its a string array, add it to the dialog one line at a time.
		// (nulls are treated as blank lines)
		else if (message instanceof String[])  {
		  String mes[] = (String[]) message;
			if (mes.length == 0)                   // The array is empty
   			add(new JLabel(new String(" ")), gbc);  // Blank line
			else  {
    		gbc.insets = new Insets(0, 20, 2, 20);
   			add(new JLabel(new String(" ")), gbc);  // Blank line
			  for (int i = 0; i < mes.length; i++)  {
				  if (mes[i] != null)  {
					  if (mes[i] == "")
		    			add(new JLabel(new String(" ")), gbc);  // Blank line
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

    // Add the Yes and No buttons to the dialog
    Panel buttonPanel = new Panel();

    Button yesButton = new Button("Yes");
		yesButton.addActionListener(this);
		yesButton.setActionCommand("yes");
		buttonPanel.add(yesButton);

	  Button noButton = new Button("No");
		noButton.addActionListener(this);
		noButton.setActionCommand("no");
		buttonPanel.add(noButton);
		
		gbc.insets = new Insets(13, 20, 15, 20);
		add(buttonPanel, gbc);

		pack();

    // Center the dialog in the middle of the parent frame
    Dimension dialogDim = getSize();
		setLocation(parentLoc.x + ((parentDim.width - dialogDim.width) / 2), 
  		          parentLoc.y + ((parentDim.height - dialogDim.height) / 2));

		super.show();

    // Execution will stall here until a button is pressed 
		//   or the dialog is closed.

		return response;
	
	}  // End of show
	
	
	
	
	public void actionPerformed(ActionEvent ae)
	{
	  String ac = ae.getActionCommand();
		
		if (ac.equals("yes"))  {
			response = YES;
			dispose();
		}
		if (ac.equals("no"))  {
			response = NO;
			dispose();
		}
		
	}  // End of actionPerformed

}  // End of YesNoDialog class


