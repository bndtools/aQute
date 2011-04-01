package aQute.hello.impl;

import java.util.*;

import aQute.bnd.annotation.component.*;
import aQute.bnd.annotation.metatype.*;
import aQute.service.hello.*;

@Component(designate = PolyglotHello.Config.class, configurationPolicy = ConfigurationPolicy.require)
public class PolyglotHello implements HelloService {
	interface Config {
		Locale locale();

		String hello();

		String goodbye();
	}

	Config config;

	@Activate
	void activate(Map<String, Object> props) {
		config = Configurable.createConfigurable(Config.class, props);
	}

	@Override
	public String sayHello() {
		return config.hello();
	}

	@Override
	public String sayGoodbye() {
		return config.goodbye();
	}

}
