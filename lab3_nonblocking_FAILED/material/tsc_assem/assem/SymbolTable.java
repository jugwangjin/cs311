/*
© 2000, Performance Evaluation Laboratory, Brigham Young University. Reproduction of all or part of this work is permitted for non-profit educational
or research use provided this copyright notice remains intact. All other rights reserved. 
*/

/*  The symbol table holds an "infinite" number of symbols
    and their addresses.  The symbol strings are case 
		sensitive. */

import java.util.Vector;

class SymbolTable
{
  private Vector table;
	
	
	public SymbolTable()
	{
		table = new Vector(10, 1);
	}  // End of constructor




  /* Returns true if the symbol is in the table.  Returns
	   false otherwise.  The symbol string cannot be null. 
		 The search IS case sensitive. */
	public boolean isSymbolInTable(String symbol)
	{
		for (int i = 0; i < table.size(); i++)
		  if (((TableEntry) table.get(i)).symbol.equals(symbol))
			  return true;
		return false;
	}  // End of isSymbolInTable




  /* Returns true if the symbol is successfully inserted in the 
	   table.  Returns false if the symbol already exists in the
		 table.  The symbol string cannot be null. */
	public boolean insertSymbol(String symbol, int addr)
	{
		if (!isSymbolInTable(symbol))  {
		  table.add(new TableEntry(symbol, addr));
			return true;
		}
	  return false;
	}  // End of insertSymbol




  /* Returns the address of the given symbol.  isSymbolInTable()
	   must be called before this method to ensure that the 
		 address is there.  -1 is returned if the symbol is not 
     there or the string is null.  */
	public int getAddress(String symbol)
	{
		if (symbol == null)  return -1;
		
		for (int i = 0; i < table.size(); i++)  {
			TableEntry te = (TableEntry) table.get(i);
		  if (te.symbol.equals(symbol))
			  return te.address;
		}
		return -1;
	}  // End of getAddress




  /* Returns a String representing the 'n'th symbol in the table. 
	   If 'n' is out of range, null is returned.  */
	public String toString(int n)
	{
	  if ((n >= table.size()) || (n < 0))
      return null;
		TableEntry entry = (TableEntry) table.get(n);
		return entry.symbol + "  =  " + entry.address + " : 0x"  
		        + Tools.shortToHexString((short) entry.address);
  }  // End of toString




  /* This nested class is the individual entry in the symbol table. */
	private class TableEntry
	{
	  String symbol;
		int address;
		
		public TableEntry(String symbol, int address)
		{
		  this.symbol = symbol;
			this.address = address;
		}  // End of constructor
	
	}  // End of TableEntry class
	
}  // End of SymbolTable class

