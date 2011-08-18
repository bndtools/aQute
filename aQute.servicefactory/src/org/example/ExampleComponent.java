package org.example;

import org.osgi.service.component.*;
import org.osgi.service.event.*;

import aQute.bnd.annotation.component.*;

@Component(servicefactory=true, properties= EventConstants.EVENT_TOPIC +"=*")
public class ExampleComponent implements EventHandler {

	@Activate
	public void activate(ComponentContext context) {
		System.out.println(context.getUsingBundle());
	}

	@Override
	public void handleEvent(Event event) {
		System.out.println(event.getTopic());
	}
}

