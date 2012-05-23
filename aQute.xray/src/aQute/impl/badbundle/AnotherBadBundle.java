package aQute.impl.badbundle;

import aQute.bnd.annotation.component.*;

@Component(immediate=true)
public class AnotherBadBundle implements Runnable {


	@Activate
	 void activate() {
		//throw new RuntimeException("Helllooo");
	}
	public void run() {
		// TODO Auto-generated method stub
		
	}
	
	
}
