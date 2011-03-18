package aQute.metatype.samples;

import java.util.*;

import org.osgi.service.cm.*;

import aQute.bnd.annotation.component.*;
import aQute.bnd.annotation.metatype.*;

@Component(designate=Config.class)
public class Echo2 {
	
	@Activate
	void activate(Map<?,?> properties) throws ConfigurationException {
		Config config = Configurable.createConfigurable(Config.class, properties);
		System.out.println(config.port());
	}

}
