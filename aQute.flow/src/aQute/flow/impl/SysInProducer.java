package aQute.flow.impl;

import java.io.*;

import aQute.bnd.annotation.component.*;
import aQute.service.flow.*;

/**
 * Produces from the System in.
 */
@Component(factory = "aQute.service.flow.Producer/sys.in")
public class SysInProducer implements Producer<String> {
	Sink<String>	sink;

	@Override
	public void setSink(Sink<String> sink) {
		this.sink = sink;

	}

	@Override
	public void produce() throws IOException {
		System.out.println("Type 'quit' to quit");
		BufferedReader rdr = new BufferedReader(new InputStreamReader(System.in));
		while (!Thread.currentThread().isInterrupted()) {
			String line = rdr.readLine();
			if (line == null || line.equals("quit"))
				return;

			sink.consume(line);
		}
	}

}
