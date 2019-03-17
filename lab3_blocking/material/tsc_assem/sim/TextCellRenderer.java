/*
© 2000, Performance Evaluation Laboratory, Brigham Young University. Reproduction of all or part of this work is permitted for non-profit educational
or research use provided this copyright notice remains intact. All other rights reserved. 
*/

import java.awt.*;
import javax.swing.*;



/* This class is a CellRenderer that eliminates any of the 
   overhead that the DefaultListCellRenderer (a JLabel) adds thus 
	 improving the performance of displaying data with a JList.  
	 Only left justified strings are displayed, and cells have a 
	 fixed preferred height and width.
	 Written by Hans Muller and taken from the Swing Connection.  */

class TextCellRenderer extends JComponent implements ListCellRenderer 
{
  private String text;
  private final int borderWidth = 1;
  private final int baseline;
  private final int height;
	private boolean isSelected = false;




  TextCellRenderer(FontMetrics metrics) 
	{
		super();
		baseline = metrics.getAscent() + borderWidth;
		this.height = metrics.getHeight() + (2 * borderWidth);
  }  // End of constructor




  /* Return the renderers fixed size here.  */
  public Dimension getPreferredSize() 
	{
		return new Dimension(0, height);
		// Note that the width is irrelevant so 0 is sent
  }  // End of getPreferredSize()




  /* Completely bypass all of the standard JComponent painting machinery.
     This is a special case: the renderer is guaranteed to be opaque,
     it has no children, and it's only a child of the JList while
     it's being used to rubber stamp cells.
     Clear the background and then draw the text. */
  public void paint(Graphics g) {
	  /* Adding this "if statement" around the two background lines improved 
		   performance by about 300%.  The default background color is used 
			 when no color is given so these two lines of code are only needed 
			 if the line of text is highlighted.  */
		if (isSelected)  {
			g.setColor(getBackground());
			g.fillRect(0, 0, getWidth(), getHeight());
		}
		g.setColor(getForeground());
		g.drawString(text, borderWidth, baseline);
  }  // End of paint




  /* This is is the ListCellRenderer method.  It just sets
   * the foreground and background properties and updates the
   * local text field.
   */
  public Component getListCellRendererComponent(JList list, Object value,
                      int index, boolean isSelected, boolean cellHasFocus) 
  {
	  this.isSelected = isSelected;
		if (isSelected) {
		  setBackground(list.getSelectionBackground());
		  setForeground(list.getSelectionForeground());
		}
		else {
		  setBackground(list.getBackground());
		  setForeground(list.getForeground());
		}
		text = value.toString();
	
		return this;
		
  }  // End of getListCellRendererComponent
	
}  // End of TextCellRenderer class

