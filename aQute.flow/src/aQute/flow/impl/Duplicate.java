package aQute.flow.impl;

import java.util.*;

import aQute.bnd.annotation.component.*;
import aQute.bnd.annotation.metatype.*;
import aQute.service.flow.*;

/**
 * Duplicates strings.
 */

@Component(factory = "aQute.service.flow.Pipe/duplicate")
public class Duplicate implements Pipe<String,String> {
	Sink<String>	sink;

	interface Config {
		int $0();
	}

	Config	config;

	@Activate
	void activate(Map<String,Object> props) {
		config = Configurable.createConfigurable(Config.class, props);
	}

	@Override
	public void consume(String t) {
		int count = config.$0();
		if (count == 0)
			count = 2;

		for (int i = 0; i < count; i++)
			sink.consume(t);
	}

	@Override
	public void setSink(Sink<String> out) {
		this.sink = out;
	}
}
