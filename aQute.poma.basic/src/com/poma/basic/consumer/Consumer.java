package com.poma.basic.consumer;

import aQute.bnd.annotation.component.*;

import com.poma.service.basic.*;

@Component
public class Consumer {

	@Reference
	void setInterface(Interface intf) {
		intf.doIt();
	}
}
