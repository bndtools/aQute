package aQute.ant.connect.hello.activator;

import java.util.*;

import org.apache.tools.ant.*;
import org.osgi.framework.*;


public class Activator implements BundleActivator {

	public static class HelloTask extends Task {
		String message = "Hello Activator";
		
		public void execute() {
			System.out.println(message);
		}
		
		public void setMessage(String m) {
			this.message = m;
		}
	}
	
	@Override
	public void start(BundleContext context) throws Exception {
		Properties props = new Properties();
		props.put("ant", "helloActivator");
		context.registerService(Class.class.getName(), HelloTask.class, props);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
	}

}
