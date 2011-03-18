package aQute.metatype.samples;

import java.util.*;

import org.osgi.service.cm.*;

import aQute.bnd.annotation.component.*;
import aQute.bnd.annotation.metatype.*;

@Component(properties="service.pid=aQute.metatype.samples.Config")
public class Echo implements ManagedService {
	
	@Override
	public void updated(Dictionary properties) throws ConfigurationException {
		if ( properties != null ) {
			Config config = Configurable.createConfigurable(Config.class, properties);
			System.out.println(config.port());
		}
	}

}
