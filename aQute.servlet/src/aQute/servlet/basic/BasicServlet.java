package aQute.servlet.basic;

import java.io.*;

import javax.servlet.http.*;

import org.osgi.service.http.*;

import aQute.bnd.annotation.component.*;

/**
 * A minimal Servlet example using components. This example registers a servlet
 * on http://localhost:??/basic and it maps all files in the STATIC bundle
 * directory in the bundle to /basic/static.
 */

@Component
public class BasicServlet extends HttpServlet {
	private static final long	serialVersionUID	= 1L;

	/**
	 * Standard Http Servlet doGet method. We only return "Hello World"
	 */
	public void doGet(HttpServletRequest req, HttpServletResponse rsp) throws IOException {
		rsp.getWriter().println("Hello World");
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
		http.registerServlet("/basic", this, null, null);
		http.registerResources("/basic/static", "STATIC", null);
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
		http.unregister("/basic");
		http.unregister("/basic/static");
	}
}
