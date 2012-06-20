package aQute.metatype.components;

import java.util.*;

import aQute.bnd.annotation.component.*;
import aQute.bnd.annotation.metatype.*;

@Component(designate = BasicComponent.Config.class, configurationPolicy = ConfigurationPolicy.require)
public class BasicComponent {
	interface Config {
		String message();
	}

	Config	config;

	@Activate
	void activate(Map<String,Object> props) {
		modified(props);
		System.out.println("Hi: " + config.message());
	}

	@Deactivate
	void deactivate() {
		System.out.println("Bye: " + config.message());
	}

	@Modified
	void modified(Map<String,Object> props) {
		config = Configurable.createConfigurable(Config.class, props);
	}

}
