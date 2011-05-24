package aQute.gwt.greeter.shared;

import com.google.gwt.user.client.rpc.*;

/**
 * The client side service interface.
 */
@RemoteServiceRelativePath("greet.dispatch")
public interface GreetingService {
	String greetServer(String name) throws IllegalArgumentException;
	
	void testTypes(int n, int nn[], Custom x);
}
