package aQute.gwt.greeter.server;

import org.osgi.framework.*;

import aQute.bnd.annotation.component.*;
import aQute.gwt.greeter.shared.*;

@Component(properties = "service.exported.interfaces=*")
public class Greeter implements GreetingService {
	String framework;
	
	@Activate
	void activate(BundleContext context) {
		framework = context.getBundle(0).getSymbolicName();
	}
	
	@Override
	public String greetServer(String name) throws IllegalArgumentException {
		return "Parlez vous Français " + name + "?<br/>You're running on: <b>" + framework + "</b>";
	}
}
