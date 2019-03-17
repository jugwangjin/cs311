/*
� 2000, Performance Evaluation Laboratory, Brigham Young University. Reproduction of all or part of this work is permitted for non-profit educational
or research use provided this copyright notice remains intact. All other rights reserved. 
*/

import java.awt.*;
import java.io.File;
import javax.swing.*;


/* Provides methods for obtaining the filename for 
   loading and saving ROMs and selecting input and 
	 output files.  The fileType is the desired extension 
	 of the file i.e. rom, out, etc. */

class FileTools
{
  private static String errorString[] = new String[2];
  private String currentDir;




  public FileTools(String currentDir)
	{
	  if (currentDir == null)
		  this.currentDir = System.getProperty("user.dir");
		else
		  this.currentDir = currentDir;
	}  // End of constructor
	
	
	
	
	/* Returns the File chosen by the user or null if no 
     file was chosen or an error occurred.  'fileType' is 
		 is the desired extension of the file to be opened. 
		 If 'fileType' is an empty string, then any extension 
		 is accepted. */
  public File openFile(Frame display, String fileType, String title)
	{
	  FileDialog fileChooser = new FileDialog(display, title);
		fileChooser.setDirectory(currentDir);
    fileChooser.show();              // Prompt for a file

		String filename = fileChooser.getFile();

    if (filename != null)  {         // A file was chosen
			currentDir = fileChooser.getDirectory();
			fileChooser = null;            // Dispose of fileChooser
  		File file = new File(currentDir, filename);
  		if (file.canRead())  {         // File is accessible
			  if ((fileType != null) && (fileType.length() != 0)) {  
				  // A file type was specified
				  if (fileType.equalsIgnoreCase(getExtension(file.getPath())))
       		  return file;
					else {                     // Wrong file type was chosen
						errorString[0] = file.getAbsolutePath();
						errorString[1] = "is not a " + fileType + " file";
  	        new OkDialog(display, true).show("Open File Error", errorString);
    				return null;
    			}
				}
				else                         // File type was not specified
				  return file;
			}
			else  {                        // File is not accessible
				errorString[0] = file.getAbsolutePath();
				errorString[1] = "is not accessible";
        new OkDialog(display, true).show("Open File Error", errorString);
				return null;
			}
    } 
  	else   {                         // A file was not chosen
			fileChooser = null;            // Dispose of fileChooser
      return null;
		}

	}  // End of openFile




  /* Returns the File chosen by the user or null if no 
     file was chosen or an error occurred.  'fileType is 
		 is the desired extension of the file to be saved. 
		 If 'fileType' is an empty string, then any extension 
		 is excepted.	*/
  public File saveFile(Frame display, String fileType, String title)
	{
	  FileDialog fileChooser = new FileDialog(display, 
		                                 title, FileDialog.SAVE);
		fileChooser.setDirectory(currentDir);
    fileChooser.show();

		String filename = fileChooser.getFile();

    if (filename != null)  {         // A file was chosen
			currentDir = fileChooser.getDirectory();
			fileChooser = null;            // Dispose of fileChooser
  		File file = new File(currentDir, filename);
			filename = file.getAbsolutePath();
		  if ((fileType != null) && (fileType.length() != 0)) {  
			  // A file type was specified
        if (fileType.equalsIgnoreCase(getExtension(filename)))
 					return file;
		   	else {                       // Wrong file type was chosen
					errorString[0] = filename;
					errorString[1] = "is not a " + fileType + " file";
	        new OkDialog(display, true).show("Save File Error", errorString);
   				return null;
   			}
 			}
		  else                          // File type was not specified
			  return file;
    } 
   	else  {                         // A file was not chosen
			fileChooser = null;           // Dispose of fileChooser
      return null;
		}

	}  // End of saveFile




  /* Returns the File chosen by the user or null if no 
     file was chosen or an error occurred.  If the extension 
		 is not .rom, null is returned.  If no extension exists, 
		 then .rom is added.    */
  public File saveRomFile(Frame display)
	{
	  FileDialog fileChooser = new FileDialog(display, "Burn To A ROM", 
		                                               FileDialog.SAVE);
		fileChooser.setDirectory(currentDir);
    fileChooser.show();

		String filename = fileChooser.getFile();

    if (filename != null)  {           // A file was chosen
			currentDir = fileChooser.getDirectory();
			fileChooser = null;    // Dispose of fileChooser
			String extension = getExtension(filename);
			if (extension == null) {
			  return new File(currentDir, filename + ".rom");
			}
			else 	{
        if (extension.equalsIgnoreCase("ROM"))
 					return new File(currentDir, filename);
		   	else {                    // Wrong extension was chosen
					errorString[0] = filename;
					errorString[1] = "is not a ROM file";
	        new OkDialog(display, true).show("Burn ROM File Error", errorString);
   				return null;
   			}
 			}
    } 
   	else  {                    // A file was not chosen
			fileChooser = null;      // Dispose of fileChooser
      return null;
		}

	}  // End of saveRomFile




  /* Returns the extension of a file name (not including the '.'). 
	   The filename cannot be null. */
  public static String getExtension(String filename) {
	  int i = filename.lastIndexOf('.');
	  if(i > 0 &&  i < filename.length() - 1) {
	    return filename.substring(i+1);
	  }
	  return "";
  }  // End of getExtension

}  // End of FileTools class

