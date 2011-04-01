package osgi.gwt.test.client;

import com.google.gwt.user.client.rpc.*;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("greet.dispatch")
public interface GreetingService extends RemoteService {
	String greetServer(String name) throws IllegalArgumentException;
}
