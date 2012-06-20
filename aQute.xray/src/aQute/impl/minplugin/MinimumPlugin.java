package aQute.impl.minplugin;

import java.io.*;
import java.net.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.felix.webconsole.*;

import aQute.bnd.annotation.component.*;

@Component(provide = {
	Servlet.class
}, properties = {
	"felix.webconsole.label=" + MinimumPlugin.PLUGIN
})
public class MinimumPlugin extends AbstractWebConsolePlugin {
	private static final long	serialVersionUID	= 1L;
	final static String			PLUGIN				= "min";

	@Override
	public String getLabel() {
		return PLUGIN;
	}

	@Override
	public String getTitle() {
		return "Minimum";
	}

	@Override
	protected void renderContent(HttpServletRequest rq, HttpServletResponse rsp) throws ServletException, IOException {
		rsp.getWriter().println("Hello World");
	}

	public URL getResource(String resource) {
		if (resource.equals("/" + PLUGIN))
			return null;

		resource = resource.replaceAll("/" + PLUGIN + "/", "");
		return getClass().getResource(resource);
	}
}
