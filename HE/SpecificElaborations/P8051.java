import reflexion_test.Processor;
import reflexion_test.SpecificConfigReader;


//must be public
public class P8051 extends AbstractProcessor {
	
	int i2cClk;
	int spiClk;
	SpecificConfigReader ConfRdr;
	Processor P;//model object

	public P8051(Processor p){
		P = p;
	}
	
	public void setConfigReader(SpecificConfigReader cr){
		ConfRdr = cr;
		System.out.println("ConfigReader is set!");
		
		config();
	}

	private void config(){
		i2cClk = (Integer)ConfRdr.getProperty("c");
	}
	
//	public void setI2cClk(int p){
//		i2cClk = p;
//	}
//	
//	public void setspiClk(int p){
//		spiClk = p;
//	}

	public int getI2cClk(){
		return i2cClk;
	}

	public int get(){
		return spiClk;
	}

	public void elaborate(){
			System.out.println("Elaborating!");
	}
}

