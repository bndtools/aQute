package aQute.impl.badbundle;

import org.osgi.service.event.*;

import aQute.bnd.annotation.component.*;

@Component
public class BadBundle {

	@Reference
	void setEventAdmin(EventAdmin ea) {
		
	}
}
