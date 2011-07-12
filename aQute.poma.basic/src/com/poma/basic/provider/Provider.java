package com.poma.basic.provider;

import aQute.bnd.annotation.component.*;

import com.poma.service.basic.*;

@Component
public class Provider implements Interface {

	@Override
	public void doIt() {
		System.out.println("Doing it!");
	}

}
