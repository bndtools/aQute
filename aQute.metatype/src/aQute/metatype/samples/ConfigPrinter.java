package aQute.metatype.samples;

import java.lang.reflect.*;
import java.util.*;

import org.osgi.service.cm.*;

import aQute.bnd.annotation.component.*;
import aQute.bnd.annotation.metatype.*;

@Component(properties="service.pid=aQute.metatype.samples.SampleConfig")
public class ConfigPrinter implements ManagedService {
	SampleConfig cnf;
	
	@SuppressWarnings("unchecked")
	@Override
	public void updated(Dictionary properties) throws ConfigurationException {
		cnf = Configurable.createConfigurable(SampleConfig.class, properties);
		
		Method ms[] = SampleConfig.class.getMethods();
		for ( Method m : ms ) {
			try {
				Object o = m.invoke(cnf);
				if ( o != null && o.getClass().isArray()) {
					Class ct = o.getClass().getComponentType();
					if ( ct == boolean.class)
						o = Arrays.toString((boolean[]) o);
					else if ( ct == byte.class)
						o = Arrays.toString((byte[]) o);
					else if ( ct == char.class)
						o = Arrays.toString((char[]) o);
					else if ( ct == short.class)
						o = Arrays.toString((short[]) o);
					else if ( ct == int.class)
						o = Arrays.toString((int[]) o);
					else if ( ct == long.class)
						o = Arrays.toString((long[]) o);
					else if ( ct == float.class)
						o = Arrays.toString((float[]) o);
					else if ( ct == double.class)
						o = Arrays.toString((double[]) o);
					else 
						o = Arrays.toString((Object[])o);
				}
				System.out.printf("%-40s %s\n", m.getName(), o);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
