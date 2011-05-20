package aQute.gwt.greeter.shared;

import com.google.gwt.user.client.rpc.*;

/**
 * The async counterpart of {@link GreetingService}
 */
public interface GreetingServiceAsync {
	void greetServer(String name, AsyncCallback<String> callback);

	void testTypes(int n, int nn[], Custom x, AsyncCallback<Void> callback);
}
