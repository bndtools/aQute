package aQute.ma.servlet;

import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.osgi.service.cm.*;

import aQute.bnd.annotation.component.*;
import aQute.config.export.*;

@Component(provide = Servlet.class, properties = "alias=/x-osgi-config-data")
public class ConfigAdminImportExportServlet extends HttpServlet {
	private static final long	serialVersionUID	= 1L;
	ConfigurationAdmin			admin;

	public void doGet(HttpServletRequest rq, HttpServletResponse rsp) throws IOException {

		rsp.setContentType("application/json");
		try {
			Exporter.toJSON(admin, rsp.getWriter(), null);
		}
		catch (Exception e) {
			e.printStackTrace(rsp.getWriter());
		}
		rsp.getWriter().flush();
	}

	@Reference
	void setConfigurationAdmin(ConfigurationAdmin c) {

		this.admin = c;
	}

}
