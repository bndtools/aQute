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
	}

	public Iterable< ? extends Program> getProgram(POptions o) throws Exception {
		return library.find("bsn=*");
	}

	public Iterable< ? extends Program> getProgram(String s) throws Exception {
		return library.find("bsn=" + s);
	}

	public String getShit() {
		return "shit";
	}

	@Reference
	void setLibrary(Library lib) {
		this.library = lib;
	}
}
