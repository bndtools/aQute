package aQute.impl.badbundle;

import org.osgi.service.event.*;

import aQute.bnd.annotation.component.*;

@Component
public class BadBundle implements Runnable {

	@Reference
	void setEventAdmin(EventAdmin ea) {
		
	}

	public void run() {
		// TODO Auto-generated method stub
		
	}
}
