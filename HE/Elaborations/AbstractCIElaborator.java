public abstract class AbstractCIElaborator extends AbstractElaborator {
    CCI target = null;    
    
    public AbstractCIElaborator(CCI  comp){      
        target = comp;
    }
    
    void Elaboration(){
       generate();
     getElaborator(target.SCa).elaboration();
getElaborator(target.SCe).elaboration();
     	
    	}
	
	}
