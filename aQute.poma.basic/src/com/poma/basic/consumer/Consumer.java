package com.poma.basic.consumer;

import com.poma.service.basic.*;

import aQute.bnd.annotation.component.*;

@Component
public class Consumer {

	
	@Reference
	void setInterface(Interface intf) {
		intf.doIt();
	}
}
