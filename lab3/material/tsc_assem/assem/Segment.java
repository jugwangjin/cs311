/*
© 2000, Performance Evaluation Laboratory, Brigham Young University. Reproduction of all or part of this work is permitted for non-profit educational
or research use provided this copyright notice remains intact. All other rights reserved. 
*/

	/*  This class represents a segment in the segment manager's 
	    segment list.  It holds the beginning and ending 
			addresses of the memory space used by the segment.  If 
			'end' < 'begin' when the class is instantiated, then 
			'end' is set to 'begin'.   */
			
class Segment
{
	int begin;
	int end;

	public Segment(int begin, int end)
	{
		if (begin < end)  {
			this.begin = begin;
			this.end = end;
		}
		else 
			this.begin = this.end = begin;

	}  // End of constructor




  /* Returns true if 'seg' overlaps this segment.  Otherwise 
	   false is returned.  'seg' cannot be null.  */
	public boolean isOverlapping(Segment seg)
	{
	  if (((seg.begin >= begin) && (seg.begin <= end)) ||
		    ((seg.end >= begin) && (seg.end <= end))  ||
				((begin >= seg.begin) && (begin <= seg.end)))
		  return true;
		else return false;
	}  // End of isOverlapping
	
	
	
	
	/*  Returns a string representing this segment. */
	public String toString()
	{
	  return  "Begin = " + begin + " : 0x" + Tools.shortToHexString((short) begin)
		        + "    End = " + end + " : 0x" + Tools.shortToHexString((short) end) 
			                                       + "    Size = " + (end - begin + 1);
	}  // End of toString

}  // End of Segment class


