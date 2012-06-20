package aQute.flow.impl;

import aQute.bnd.annotation.component.*;
import aQute.service.flow.*;

/**
 * Sinks everything from system.out
 */
@Component(factory = "aQute.service.flow.Sink/sys.out")
public class SysOutSink implements Sink<String> {
	@Override
	public void consume(String t) {
		System.out.println(t);
	}
}
