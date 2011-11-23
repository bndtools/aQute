package aQute.service.ant;

import java.lang.reflect.*;
import java.util.*;

import org.apache.tools.ant.*;
import org.osgi.framework.*;

public class ConnectTask extends Task {
	static BundleContext bundleContext;
	final static Map<Class<?>,Tasker<Task>> taskFactories = new HashMap<Class<?>,Tasker<Task>>();
	
	public static Class<?> register(Tasker<?> tf) {
		for ( Type type : tf.getClass().getGenericInterfaces()) {
			if ( type instanceof ParameterizedType ) {
				ParameterizedType pt = (ParameterizedType) type;
				Type taskType = pt.getActualTypeArguments()[0];
				Class<?> taskClass = (Class<?>) taskType;
				taskFactories.put(taskClass, (Tasker<Task>) tf);
				System.out.println("tf " + taskClass + " " + tf);
				return taskClass;
			}
		}
		return null;
	}
	
	public static void unregister(Tasker<?> tf) {
		taskFactories.values().remove(tf);
	}
	public void execute() throws BuildException {
		Tasker<Task> tf = taskFactories.get(getClass());
		tf.execute(this);
	}
}
