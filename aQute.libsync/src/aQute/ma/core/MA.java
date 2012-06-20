package aQute.ma.core;

import java.util.*;

import org.osgi.framework.*;

import aQute.bnd.annotation.component.*;

@Component
public class MA {

	@Activate
	void activate(BundleContext context) throws InvalidSyntaxException {
		ServiceReference[] refs = context.getServiceReferences((String) null, "(launcher.arguments=*)");
		if (refs == null)
			throw new RuntimeException("No main arguments found for service with (launcher.arguments=*)");
		String[] args = (String[]) refs[0].getProperty("launcher.arguments");

		System.out.println("Hello world! " + Arrays.toString(args));
	}
}
