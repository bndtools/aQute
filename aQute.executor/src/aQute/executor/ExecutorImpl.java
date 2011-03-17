package aQute.executor;

import java.util.*;
import java.util.concurrent.*;

import aQute.bnd.annotation.component.*;

@Component
public class ExecutorImpl implements Executor {
	ExecutorService es;
	
	interface Config {
		enum Type { FIXED, CACHED, SINGLE }
		int ranking();
		Type type();
		String[] id();
		int size();
	}
	
	@Activate
	void activate(Map<String,Object> properties) {

	}

	@Deactivate
	void deactivate() {
		es.shutdownNow();
	}
	
	@Override
	public void execute(Runnable command) {
		
	}

}
