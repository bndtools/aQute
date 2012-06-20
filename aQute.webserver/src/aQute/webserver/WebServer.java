package aQute.webserver;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.servlet.http.*;

import org.osgi.service.http.*;

import aQute.bnd.annotation.component.*;
import aQute.bnd.annotation.metatype.*;
import aQute.webserver.WebServer.Config;

@Component(provide = {}, configurationPolicy = ConfigurationPolicy.require, immediate = true, designateFactory = Config.class)
public class WebServer implements HttpContext {

	interface Config {
		String alias();

		File[] directories();
	}

	Config		config;
	HttpService	http;

	@Activate
	void activate(Map<String,Object> props) throws NamespaceException {
		this.config = Configurable.createConfigurable(Config.class, props);
		http.registerResources(config.alias(), "", this);
	}

	@Reference
	void setHttp(HttpService http) throws NamespaceException {
		this.http = http;
	}

	public boolean handleSecurity(HttpServletRequest request, HttpServletResponse response) throws IOException {
		return true;
	}

	public URL getResource(String name) {
		for (File file : config.directories()) {
			name = name.replace('/', File.separatorChar);
			File f = new File(file, name);
			if (f.isFile()) {
				try {
					return f.toURI().toURL();
				}
				catch (MalformedURLException me) {
					// Cannot be thrown
				}
			}
		}
		return null;
	}

	public String getMimeType(String name) {
		return null;
	}
}