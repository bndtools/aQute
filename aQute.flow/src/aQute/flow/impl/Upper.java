package aQute.flow.impl;

import aQute.bnd.annotation.component.*;
import aQute.service.flow.*;

/**
 * Makes every string upper case.
 */
@Component(factory = "aQute.service.flow.Pipe/upper")
public class Upper implements Pipe<String,String> {
	Sink<String>	sink;

	@Override
	public void consume(String t) {
		sink.consume(t.toUpperCase());
	}

	@Override
	public void setSink(Sink<String> out) {
		this.sink = out;
	}
}
