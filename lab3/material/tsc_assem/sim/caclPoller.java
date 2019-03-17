/*
© 2000, Performance Evaluation Laboratory, Brigham Young University. Reproduction of all or part of this work is permitted for non-profit educational
or research use provided this copyright notice remains intact. All other rights reserved. 
*/

class calcPoller extends Thread
{

	final static int DATA = Constants.DATA;

	private int DATASENTLOC = 61170;
	private int ANSWERLOC = 61169;
	private Calc calc;
	private Memory mem;

	public calcPoller(Calc calc, Memory mem)
	{
		super("calcPoller");
		this.calc = calc;
		this.mem = mem;
	}

	public void run()
	{
		while(mem.readMem(DATASENTLOC, DATA) != 65535)
		{
			yield();				
		}			
		calc.displayString = "" + mem.readMem(ANSWERLOC, DATA);
		calc.calcDisplay.setText(calc.displayString);
  	calc.resetMem();
	}
	
}//calcPoller class
