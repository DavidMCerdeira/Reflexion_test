package reflexion_test;

public class Processor {
	protected int Clock;
	protected int Width;
	
	public boolean setClock(int value){
		Clock = value;
		return true;
	}

	public boolean setWidth(int value){
		Width = value;
		return true;
	}
	
	public int getClock(){ return Clock;}
	
	public int getWidth(){ return Width;}
}
