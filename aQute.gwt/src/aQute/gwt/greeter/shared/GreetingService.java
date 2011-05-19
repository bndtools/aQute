package aQute.gwt.greeter.shared;

import com.google.gwt.user.client.rpc.*;

/**
 * The client side service interface.
 */
@RemoteServiceRelativePath("greet.dispatch")
public interface GreetingService extends RemoteService {
	String greetServer(String name) throws IllegalArgumentException;
}
