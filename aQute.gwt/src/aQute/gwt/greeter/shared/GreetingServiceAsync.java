package aQute.gwt.greeter.shared;

import com.google.gwt.user.client.rpc.*;

/**
 * The async counterpart of {@link GreetingService}
 */
public interface GreetingServiceAsync {
	void greetServer(String input, AsyncCallback<String> callback)
			throws IllegalArgumentException;
}
