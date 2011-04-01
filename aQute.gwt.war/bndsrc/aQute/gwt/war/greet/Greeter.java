package aQute.gwt.war.greet;

import osgi.gwt.test.client.*;
import aQute.bnd.annotation.component.*;

@Component(properties = "service.exported.interfaces=*")
public class Greeter implements GreetingService {
	@Override
	public String greetServer(String name) throws IllegalArgumentException {
		return "Parlez vous Français " + name + "?";
	}
}
