package aQute.gwt.dispatcher;

import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.osgi.framework.*;
import org.osgi.service.http.*;

import aQute.bnd.annotation.component.*;
import aQute.gwt.dispatcher.gwtimpl.SerializabilityUtil;
import aQute.gwt.dispatcher.gwtimpl.ServerSerializationStreamReader;

import com.google.gwt.user.client.rpc.*;
import com.google.gwt.user.client.rpc.impl.*;
import com.google.gwt.user.server.rpc.*;
import com.google.gwt.user.server.rpc.impl.*;

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

	class Request {
		RPCRequest request;
		Object delegate;
		ServiceReference reference;

		public String invoke() throws SerializationException {
			try {
				return RPC.invokeAndEncodeResponse(delegate,
						request.getMethod(), request.getParameters(),
						request.getSerializationPolicy(), request.getFlags());
			} finally {
				context.ungetService(reference);
			}
		}

		public void setRPCRequest(RPCRequest rq) {
			this.request = rq;
		}

		public Object getDelegate() {
			return delegate;
		}
	}

	/**
	 * Activate this component. We only need the BundleContext of our bundle.
	 * 
	 * @param context
	 *            our bundle's context
	 * @throws Exception
	 */

	@Activate
	void activate(BundleContext context) throws Exception {
		this.context = context;
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
			Request request = decodeRequest(payload);
			onAfterRequestDeserialized(request.request);
			return request.invoke();
		} catch (Exception ex) {
			ex.printStackTrace();
			log("An IncompatibleRemoteServiceException was thrown while processing this call.",
					ex);
			return RPC.encodeResponseForFailure(null, ex);
		}
		
	}

	/**
	 * Return the delegate we've assigned to the server of the given interface
	 * name.
	 */
	Request getForDelegate(String name) {
		Request request = new Request();

		request.reference = context.getServiceReference(name);
		if (request.reference == null)
			throw new IllegalStateException("No service available for " + name);

		if (!isExported(request.reference, name))
			throw new SecurityException("Service for " + name
					+ " is not intended to be used remotely");

		request.delegate = context.getService(request.reference);
		return request;
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
	 * Decode the incoming request
	 * 
	 * @param encodedRequest
	 * @param type
	 * @param serializationPolicyProvider
	 * @return
	 */
	Request decodeRequest(String encodedRequest) throws Exception {
		if (encodedRequest.length() == 0)
			throw new IllegalArgumentException("encodedRequest cannot be empty");

		// The first token is inside the GWT domain. So our class loader needs to be OUR
		// class loader.
		ServerSerializationStreamReader streamReader = new ServerSerializationStreamReader(
				GWTDispatchServlet.class.getClassLoader(), null);
		streamReader.prepareToRead(encodedRequest);

		RpcToken rpcToken = null;
		if (streamReader
				.hasFlags(AbstractSerializationStream.FLAG_RPC_TOKEN_INCLUDED)) {
			// Read the RPC token
			rpcToken = (RpcToken) streamReader.deserializeValue(RpcToken.class);
		}

		// Read the name of the RemoteService interface
		String serviceIntfName = maybeDeobfuscate(streamReader,
				streamReader.readString());


		// Ask whoever it is to create a request object that
		// provides access to the delegate. Subclasses
		// should be able to override it.
		Request request = getForDelegate(serviceIntfName);

		
		// We must patch the SerializationStreamReader to use 
		// the class loader of the delegate
		ClassLoader loader = request.getDelegate().getClass().getClassLoader();
		streamReader.setClassLoader(loader);
		Class<?> serviceIntf = SerializabilityUtil.forName(serviceIntfName,
				loader);

		
		String serviceMethodName = streamReader.readString();

		int paramCount = streamReader.readInt();
		if (paramCount > streamReader.getNumberOfTokens()) {
			throw new IncompatibleRemoteServiceException(
					"Invalid number of parameters");
		}
		Class<?>[] parameterTypes = new Class[paramCount];

		for (int i = 0; i < parameterTypes.length; i++) {
			String paramClassName = maybeDeobfuscate(streamReader,
					streamReader.readString());

			parameterTypes[i] = SerializabilityUtil.forName(paramClassName,
					loader);
		}

		Method method = serviceIntf
				.getMethod(serviceMethodName, parameterTypes);

		Object[] parameterValues = new Object[parameterTypes.length];
		for (int i = 0; i < parameterValues.length; i++) {
			parameterValues[i] = streamReader
					.deserializeValue(parameterTypes[i]);
		}

		SerializationPolicy serializationPolicy = streamReader
		.getSerializationPolicy();
		request.setRPCRequest(new RPCRequest(method, parameterValues, rpcToken,
				serializationPolicy, streamReader.getFlags()));

		return request;

	}


	/**
	 * Given a type identifier in the stream, attempt to deobfuscate it. Retuns
	 * the original identifier if deobfuscation is unnecessary or no mapping is
	 * known.
	 */
	String maybeDeobfuscate(ServerSerializationStreamReader streamReader,
			String name) throws SerializationException {
		int index;
		if (streamReader
				.hasFlags(AbstractSerializationStream.FLAG_ELIDE_TYPE_NAMES)) {
			SerializationPolicy serializationPolicy = streamReader
					.getSerializationPolicy();
			if (!(serializationPolicy instanceof TypeNameObfuscator)) {
				throw new IncompatibleRemoteServiceException(
						"RPC request was encoded with obfuscated type names, "
								+ "but the SerializationPolicy in use does not implement "
								+ TypeNameObfuscator.class.getName());
			}

			String maybe = ((TypeNameObfuscator) serializationPolicy)
					.getClassNameForTypeId(name);
			if (maybe != null) {
				return maybe;
			}
		} else if ((index = name.indexOf('/')) != -1) {
			return name.substring(0, index);
		}
		return name;
	}

	/**
	 * Our dependency on Http for the case we use the Http Service with DS.
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
