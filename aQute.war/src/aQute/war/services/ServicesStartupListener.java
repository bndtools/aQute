package aQute.war.services;

import java.util.*;

import javax.servlet.*;

import org.apache.felix.http.proxy.*;
import org.osgi.framework.*;

import de.kalpatec.pojosr.framework.*;
import de.kalpatec.pojosr.framework.launch.*;

public class ServicesStartupListener implements ServletContextListener {
	private static final long serialVersionUID = 1L;
	PojoServiceRegistry sr;
	BundleContext context;
	ProxyListener pl = new ProxyListener();

	@Override
	public void contextInitialized(ServletContextEvent event) {
		System.out.println("init");

		if ( context == null ) {
			
			try {
				Properties p = new Properties();
				List<BundleDescriptor> descriptors = new ClasspathScanner()
						.scanForBundles();
				p.put(PojoServiceRegistryFactory.BUNDLE_DESCRIPTORS, descriptors);
				sr = new PojoServiceRegistryFactoryImpl().newPojoServiceRegistry(p);
				context = sr.getBundleContext();
				event.getServletContext().setAttribute(
						"org.osgi.framework.BundleContext", context);
				
				pl.contextInitialized(event);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent event) {
		try {
			pl.contextDestroyed(event);
			context.getBundle(0).stop();
		} catch (BundleException e) {
			e.printStackTrace();
		}
	}
}
