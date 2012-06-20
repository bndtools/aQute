package aQute.executor;

import static java.lang.Math.*;

import java.util.*;
import java.util.concurrent.*;

import aQute.bnd.annotation.component.*;
import aQute.bnd.annotation.metatype.*;
import aQute.executor.ExecutorImpl.Config;
import aQute.executor.ExecutorImpl.Config.Type;

@Component(designateFactory = Config.class, configurationPolicy = ConfigurationPolicy.require)
public class ExecutorImpl implements Executor {
	ExecutorService	es;

	interface Config {
		enum Type {
			FIXED, CACHED, SINGLE
		}

		int service_ranking();

		Type type();

		String id();

		int size();
	}

	Config	config;

	@Activate
	void activate(Map<String,Object> properties) {
		config = Configurable.createConfigurable(Config.class, properties);
		Type t = config.type();
		if (t == null)
			t = Config.Type.FIXED;

		switch (t) {
			case FIXED :
				es = Executors.newFixedThreadPool(max(config.size(), 2));
				break;
			case CACHED :
				es = Executors.newCachedThreadPool();
				break;
			case SINGLE :
				es = Executors.newSingleThreadExecutor();
				break;
		}
	}

	@Deactivate
	void deactivate() {
		es.shutdown();
	}

	@Override
	public void execute(Runnable command) {
		es.execute(command);
	}

}
