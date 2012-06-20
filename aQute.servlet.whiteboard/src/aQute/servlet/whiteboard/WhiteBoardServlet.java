package aQute.servlet.whiteboard;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.osgi.framework.*;
import org.osgi.service.http.*;

import aQute.bnd.annotation.component.*;

/**
 * A minimal Servlet example using components. This example registers a servlet
 * on http://localhost:??/basic and it maps all files in the STATIC bundle
 * directory in the bundle to /basic/static.
 */

@Component
public class WhiteBoardServlet extends HttpServlet {
	private static final long					serialVersionUID	= 1L;
	final SortedMap<ServiceReference,Pattern>	servlets			= new TreeMap<ServiceReference,Pattern>(
																			Collections.reverseOrder());
	BundleContext								context;

	/**
	 * Dispatches to any servlet that is registered with the url-pattern
	 * property. This property must be a valid regular expression.
	 */
	@Override
	public void service(HttpServletRequest req, HttpServletResponse rsp) throws ServletException, IOException {
		String pathInfo = req.getPathInfo();
		Set<Bundle> bundles = new HashSet<Bundle>();

		for (Map.Entry<ServiceReference,Pattern> e : servlets.entrySet()) {
			bundles.add(e.getKey().getBundle());
			Pattern p = e.getValue();
			Matcher matcher = p.matcher(pathInfo);
			if (matcher.matches()) {
				HttpServlet servlet = (HttpServlet) context.getService(e.getKey());
				try {
					servlet.service(req, rsp);
					return;
				}
				finally {
					context.ungetService(e.getKey());
				}
			}
		}

		// nothing found, look in the statics
		for (Bundle b : bundles) {
			URL url = b.getEntry("STATIC" + pathInfo);
			if (url != null) {
				copy(url.openStream(), rsp.getOutputStream());
				return;
			}
		}

		rsp.setStatus(HttpServletResponse.SC_NOT_FOUND);
	}

	private void copy(InputStream in, OutputStream out) throws IOException {
		try {
			byte[] buffer = new byte[8196];
			int size = in.read(buffer);
			while (size > 0) {
				out.write(buffer, 0, size);
				size = in.read(buffer);
			}
		}
		finally {
			in.close();
		}
	}

	@Activate
	protected void setBundleContext(BundleContext context) {
		this.context = context;
	}

	/**
	 * We have a dependency on the Http Service. We register a servlet at /basic
	 * and our resources from the STATIC bundle directory to /basic/static This
	 * method is called by the SCR when it found an Http Service.
	 * 
	 * @param http
	 *            The http service
	 * @throws Exception
	 *             when something fails
	 */
	@Reference
	void setHttp(HttpService http) throws Exception {
		http.registerServlet("/", this, null, null);
	}

	/**
	 * Unset the http service. This method is called by the SCR when it found an
	 * Http Service.
	 * 
	 * @param http
	 *            The http service
	 * @throws Exception
	 *             when something fails
	 */
	void unsetHttp(HttpService http) throws Exception {
		http.unregister("/");
	}

	/**
	 * Get our servlets in the registry
	 * 
	 * @param ref
	 * @throws Exception
	 */
	@Reference(type = '*', service = HttpService.class, target = "(url-pattern=*)")
	synchronized protected void addServlet(ServiceReference ref) throws Exception {
		String s = (String) ref.getProperty("url-pattern");
		Pattern p = Pattern.compile(s);
		servlets.put(ref, p);
	}

	synchronized protected void removeServlet(ServiceReference ref) {
		servlets.remove(ref);
	}
}
