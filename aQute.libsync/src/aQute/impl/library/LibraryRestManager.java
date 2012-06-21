package aQute.impl.library;

import aQute.bnd.annotation.component.*;
import aQute.service.library.*;
import aQute.service.library.Library.Program;
import aQute.service.rest.*;

@Component
public class LibraryRestManager implements ResourceManager {
	Library	library;

	interface POptions extends Options {
		int limit();

		String bsn();

		int start();
	}

	public Iterable< ? extends Program> getProgram(POptions o) throws Exception {
		return library.find(null, o.start(), o.limit());
	}

	public Program getProgram(String bsn) throws Exception {
		return library.getProgram(bsn);
	}

	public String getShit() {
		return "shit";
	}

	@Reference
	void setLibrary(Library lib) {
		this.library = lib;
	}
}
