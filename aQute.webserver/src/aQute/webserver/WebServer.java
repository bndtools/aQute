package aQute.webserver;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.servlet.http.*;

import org.osgi.framework.*;
import org.osgi.service.http.*;
import org.osgi.util.tracker.*;

import aQute.bnd.annotation.component.*;
import aQute.bnd.annotation.metatype.Configurable;
import aQute.webserver.WebServer.Config;

@Component(provide = {}, configurationPolicy = ConfigurationPolicy.require, immediate = true, designateFactory = Config.class)
public class WebServer implements HttpContext {

	interface Config {
		String alias();

		boolean bundles();

		File[] directories();
	}

	Config			config;
	HttpService		http;
	BundleTracker	tracker;

	@Activate
	void activate(Map<String,Object> props, BundleContext context) throws NamespaceException {
		this.config = Configurable.createConfigurable(Config.class, props);
		http.registerResources(config.alias(), "", this);
		if (config.bundles()) {
			tracker = new BundleTracker(context, Bundle.ACTIVE | Bundle.STARTING, null) {
				public Object addingBundle(Bundle bundle, BundleEvent event) {
					if (bundle.getEntryPaths("static/") != null)
						return bundle;
					return null;
				}
			};
			tracker.open();
		}
	}

	@Deactivate
	void deactivate() {
		tracker.close();
		http.unregister(config.alias());
	}

	@Reference
	void setHttp(HttpService http) throws NamespaceException {
		this.http = http;
	}

	public boolean handleSecurity(HttpServletRequest request, HttpServletResponse response) throws IOException {
		return true;
	}

	public URL getResource(String name) {
		if (name.isEmpty())
			name = "index.html";

		for (File file : config.directories()) {
			name = name.replace('/', File.separatorChar);
			File f = new File(file, name);
			if (f.isDirectory())
				f = new File(f, "index.html");

			if (f.isFile()) {
				try {
					return f.toURI().toURL();
				}
				catch (MalformedURLException me) {
					// Cannot be thrown
				}
			}
		}
		if (config.bundles()) {
			Bundle[] bundles = tracker.getBundles();
			if (bundles != null) {
				for (Bundle b : bundles) {
					URL url = b.getResource("static/" + name);
					if (url == null)
						url = b.getResource("static/" + name + "/index.html");
					return url;
				}
			}
		}
		return null;
	}

	public String getMimeType(String name) {
		return null;
	}
}