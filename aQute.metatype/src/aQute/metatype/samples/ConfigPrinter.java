package aQute.metatype.samples;

import java.lang.reflect.*;
import java.net.*;
import java.util.*;

import org.osgi.service.cm.*;

import aQute.bnd.annotation.component.*;
import aQute.bnd.annotation.metatype.*;

/**
 * This example shows different options of a configuration.
 *
 */

@Metadata.OCD
interface SampleConfig {
	String name();
	enum X { A, B, C; }
	X x();
	int birthYear();
	URI uri();
	URI[] uris();
	Collection<URI> curis();
	Collection<Integer> ints(); // does not work with webconsole
}

@Component(properties="service.pid=aQute.metatype.samples.SampleConfig")
public class ConfigPrinter implements ManagedService {
	SampleConfig cnf;
	
	@SuppressWarnings("unchecked")
	@Override
	public void updated(Dictionary properties) throws ConfigurationException {
		System.out.println("\n--- " + new Date());
		cnf = Configurable.createConfigurable(SampleConfig.class, properties);
		
		Method ms[] = SampleConfig.class.getMethods();
		for ( Method m : ms ) {
			try {
				Object o = m.invoke(cnf);
				if ( o != null && o.getClass().isArray()) {
					int length = Array.getLength(o);
					StringBuilder sb = new StringBuilder();
					sb.append('[');
					String del ="";
					for ( int i=0; i<length; i++) {
						sb.append(del);
						sb.append(Array.get(o, i));
						del = ", ";
					}
					sb.append(']');
					o = sb;
					
				}
				System.out.printf("%-40s %s\n", m.getName(), o);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
