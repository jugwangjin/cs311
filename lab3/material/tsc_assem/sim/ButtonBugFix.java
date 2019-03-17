/*
© 2000, Performance Evaluation Laboratory, Brigham Young University. Reproduction of all or part of this work is permitted for non-profit educational
or research use provided this copyright notice remains intact. All other rights reserved. 
*/

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


/*  This class fixes a bug in the abstract button class
    i.e. the setDisabledSelectedIcon() method does not
		appear to work.  
		
		If the toggle button is selected then the disabled icon is 
		set to be a red "on" icon.  Otherwise it is set to be a 
		red "off" icon.  */
 
class ButtonBugFix implements ItemListener
{
  final static ImageIcon redLightOnImage = Constants.redLightOnImage;
  final static ImageIcon redLightOffImage = Constants.redLightOffImage;


  public void itemStateChanged(ItemEvent e)
	{
	  AbstractButton source = (AbstractButton) e.getSource();
		if (source.isSelected())
		  source.setDisabledIcon(redLightOnImage);
		else
		  source.setDisabledIcon(redLightOffImage);
	}  // End of itemStateChanged

}  // End of ButtonBugFix class

