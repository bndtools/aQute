package aQute.impl.library;

import java.io.*;

public class NoCloseInputStream extends FilterInputStream {
	
	NoCloseInputStream(InputStream in) {
		super(in);
	}
	
	public void close() {
		// Ignore
	}
}
