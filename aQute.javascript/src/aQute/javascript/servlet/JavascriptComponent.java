package aQute.javascript.servlet;

import java.io.*;
import java.util.*;

import org.osgi.framework.*;
import org.osgi.service.http.*;

import aQute.bnd.annotation.component.*;
import aQute.bnd.annotation.metatype.Configurable;
import aQute.javascript.servlet.JavascriptComponent.Config;

@Component(configurationPolicy = ConfigurationPolicy.require, designate = Config.class)
public class JavascriptComponent {
	HttpService		http;
	BundleContext	context;

	interface Config {
		String alias();

		File scriptDir();

		File resourceDir();
	}

	Config	config;

	@Activate
	void activate(BundleContext context, Map<String, Object> props)
			throws Exception {
		this.context = context;
		config = Configurable.createConfigurable(Config.class, props);
		http.registerServlet(config.alias(), new JavascriptServlet(this), null,
				null);
	}

	@Reference
	void setHttp(HttpService http) {
		this.http = http;
	}
}