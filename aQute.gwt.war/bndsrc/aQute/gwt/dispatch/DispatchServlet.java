package aQute.gwt.dispatch;

import java.lang.reflect.*;
import java.util.*;
import java.util.regex.*;

import org.osgi.framework.*;

import com.google.gwt.user.client.rpc.*;
import com.google.gwt.user.server.rpc.*;

import de.kalpatec.pojosr.framework.*;
import de.kalpatec.pojosr.framework.launch.*;

/**
 * Implements a dispatching mechanism for for remote procedure requests from GWT
 * applications. It will decode the request and then from the found method
 * decode the service that is called. It will then look up the service and call
 * it.
 */
public class DispatchServlet extends RemoteServiceServlet {
	static Pattern DISPATCH = Pattern.compile("(/.+)*/([^/\\.]+)\\.dispatch");
	private static final long serialVersionUID = 1L;

	PojoServiceRegistry sr;
	BundleContext context;

	/**
	 * Initialize the service registry.
	 * 
	 * @throws Exception
	 *             when something fails
	 */
	public DispatchServlet() throws Exception {
		Properties p = new Properties();
		List<BundleDescriptor> descriptors = new ClasspathScanner()
				.scanForBundles();
		p.put(PojoServiceRegistryFactory.BUNDLE_DESCRIPTORS, descriptors);
		sr = new PojoServiceRegistryFactoryImpl().newPojoServiceRegistry(p);
		context = sr.getBundleContext();
	}

	/**
	 * This method is overridden from the basic RemoteServiceServlet. We decode
	 * the request without a class, this prevents a security check that checks
	 * if this servlet is implementing the interface. Which is of course bad
	 * coupling.
	 * 
	 * We then use the found method to the interface and try to locate a service
	 * with that interface. If found, we call the method on it.
	 */
	@Override
	public String processCall(String payload) throws SerializationException {
		try {
			RPCRequest rpcRequest = RPC.decodeRequest(payload, null, null);
			onAfterRequestDeserialized(rpcRequest);

			Method method = rpcRequest.getMethod();
			Class<?> interf = method.getDeclaringClass();
			ServiceReference ref = context
					.getServiceReference(interf.getName());

			if (ref != null) {
				if (isExported(ref, interf.getName())) {
					Object service = context.getService(ref);
					if (service != null) {
						try {
							return RPC.invokeAndEncodeResponse(service,
									rpcRequest.getMethod(),
									rpcRequest.getParameters(),
									rpcRequest.getSerializationPolicy(),
									rpcRequest.getFlags());
						} finally {
							context.ungetService(ref);
						}
					}
				}
			}
			log("Could not find GWT service for "
					+ method.getDeclaringClass().getName() + "."
					+ method.getName());
			return RPC.encodeResponseForFailure(null,
					new IncompatibleRemoteServiceException());
		} catch (IncompatibleRemoteServiceException ex) {
			log("An IncompatibleRemoteServiceException was thrown while processing this call.",
					ex);
			return RPC.encodeResponseForFailure(null, ex);
		}
	}

	private boolean isExported(ServiceReference ref, String name) {
		try {
			Object o = ref.getProperty("service.exported.interfaces");
			if (o == null)
				return false;

			if ("*".equals(o))
				return true;

			Object[] test;

			if (o instanceof Object[])
				test = (String[]) o;
			else if (o instanceof Collection)
				test = ((Collection<?>) o).toArray();
			else
				test = new Object[] { o };

			for (Object oo : test) {
				if (name.equals(oo.toString()))
					return true;
			}
			return false;
		} catch (Exception e) {
			return false;
		}
	}
}
