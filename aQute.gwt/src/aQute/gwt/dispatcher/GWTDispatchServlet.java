package aQute.gwt.dispatcher;

import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.osgi.framework.*;
import org.osgi.service.http.*;
import org.osgi.util.tracker.*;

import aQute.bnd.annotation.component.*;

import com.google.gwt.user.client.rpc.*;
import com.google.gwt.user.server.rpc.*;

/**
 * Implements a dispatching mechanism for for remote procedure requests from GWT
 * applications. It will decode the request and then from the found method
 * decode the service that is called. It will then look up the service and call
 * it.
 * <p/>
 * This class can be used in two different ways. When there is an Http Service
 * it will register as a servlet. It can also be linked from a web.xml file in a
 * WAR/WAB.
 */

@Component(provide = {}, immediate = true)
public class GWTDispatchServlet extends RemoteServiceServlet {
	private static final long serialVersionUID = 1L;

	BundleContext context;
	ServiceTracker tracker;
	HashMap<String, Class<?>> rpcMap;

	/**
	 * Activate this component. We only need the BundleContext of our bundle.
	 * 
	 * @param context
	 *            our bundle's context
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Activate
	void activate(BundleContext context) throws Exception {
		this.context = context;
		Field fs[] = RPC.class.getDeclaredFields();
		for (Field f : fs) {
			if (f.getName().equals("TYPE_NAMES")) {
				try {
					f.setAccessible(true);
					rpcMap = (HashMap<String, Class<?>>) f.get(null);
					break;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		tracker = new ExportedTracker(context, rpcMap);
		tracker.open();
	}

	/**
	 * The destroy/deactivate method. We just close our tracker.
	 */
	@Deactivate
	public void destroy() {
		tracker.close();
	}

	/**
	 * This method is called by the servlet runner if this servlet is started
	 * directly from web.xml. It expects the servlet context to contain an
	 * attribute for the bundle context with the name:
	 * "org.osgi.framework.BundleContext". This is done by the servlet bridge
	 * and the PojoSR code if an appropriate listener is registered in the
	 * web.xml.
	 */
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		if (this.context == null) {
			try {
				BundleContext context = (BundleContext) config
						.getServletContext().getAttribute(
								"org.osgi.framework.BundleContext");
				if (context != null)
					activate(context);
			} catch (Exception e) {
				throw new ServletException(e);
			}
		}
	}

	/**
	 * The standard service call. GWT uses POST methods. We allow static content
	 * for GET, so we dispatch GET methods to our own implementation and not
	 * throw an error like GWT does. This allows us to service static content.
	 */
	@Override
	public void service(HttpServletRequest req, HttpServletResponse rsp)
			throws IOException, ServletException {
		if (req.getMethod().equals("GET"))
			this.doGet(req, rsp);
		else
			super.service(req, rsp);
	}

	/**
	 * The standard doGet scans bundles that have a STATIC directory for the URI
	 * of the doGet method. The first one found will provide this content.
	 */
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse rsp)
			throws IOException, ServletException {
		String pathInfo = "STATIC" + req.getPathInfo();
		for (Bundle b : context.getBundles()) {
			if (b.getState() == Bundle.ACTIVE) {
				URL url = b.getEntry(pathInfo);
				if (url != null) {
					copy(url.openStream(), rsp.getOutputStream());
					return;
				}
			}
		}
		rsp.setStatus(HttpServletResponse.SC_NOT_FOUND);
	}

	/**
	 * Helper, copy the in to the out and close the in.
	 * 
	 * @param in
	 * @param out
	 * @throws IOException
	 */
	private void copy(InputStream in, ServletOutputStream out)
			throws IOException {
		byte[] buffer = new byte[10000];
		try {
			int size = in.read(buffer);
			while (size > 0) {
				out.write(buffer, 0, size);
				size = in.read(buffer);
			}
		} finally {
			in.close();
		}
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
			// Ensure we've the proper content
			// because the RPC class does not do this, it assumes
			// it never changes at start up.
			synchronized (rpcMap) {
			}

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

	/**
	 * Check if the ref is exporting the interface name.
	 * 
	 * @param ref
	 *            The service reference
	 * @param name
	 *            the name of the interface being called
	 * @return true if the name is one of the exported interfaces
	 */
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

	/**
	 * Our dependency on Http for the case we use the Http Service with SCR.
	 * 
	 * @param http
	 * @throws ServletException
	 * @throws NamespaceException
	 */
	@Reference
	void setHttp(HttpService http) throws ServletException, NamespaceException {
		System.out.println("Found Http service");
		http.registerServlet("/", this, null, null);
	}

}
