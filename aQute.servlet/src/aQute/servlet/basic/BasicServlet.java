package aQute.servlet.basic;

import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.osgi.service.http.*;

import aQute.bnd.annotation.component.*;

@Component
public class BasicServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	
	public void doGet(HttpServletRequest req, HttpServletResponse rsp) throws IOException {
		rsp.getWriter().println("Hello World");
	}
	
	
	@Reference
	void setHttp( HttpService http) throws ServletException, NamespaceException {
		http.registerServlet("/basic", this, null,  null);
		http.registerResources("/basic/rsrc", "STATIC", null);
	}
	void unsetHttp( HttpService http) throws ServletException, NamespaceException {
		http.unregister("/basic");
	}
}
