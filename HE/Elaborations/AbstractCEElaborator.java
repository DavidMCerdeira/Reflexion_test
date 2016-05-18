public abstract class AbstractCEElaborator extends AbstractElaborator implements AbstractIaElaborator{
    CCE target = null;    
    
    public AbstractCEElaborator(CCE  comp){      
        target = comp;
    }
    
    void Elaboration(){
       generate();
          	
    	}
	
	}
