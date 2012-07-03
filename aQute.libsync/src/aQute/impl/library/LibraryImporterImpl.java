package aQute.impl.library;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.regex.*;

import aQute.impl.library.LibraryImpl.ProgramImpl;
import aQute.impl.library.LibraryImpl.RevisionImpl;
import aQute.lib.data.*;
import aQute.lib.osgi.*;
import aQute.libg.reporter.Messages.ERROR;
import aQute.libg.reporter.*;
import aQute.libg.version.*;
import aQute.service.library.*;
import aQute.service.library.Library.Importer;
import aQute.service.library.Library.Revision;
import aQute.service.library.Library.RevisionRef;
import aQute.service.reporter.*;

public class LibraryImporterImpl extends ReporterAdapter implements Library.Importer {
	final LibraryImpl		parent;
	String					uniqueId;
	Callable<InputStream>	getter;
	boolean					existed;
	String					path;
	String					url;

	interface ImporterMessages {

		ERROR CouldNotImport_(String uniqueId);

		ERROR AlreadyImported_(String uniqueId);

		ERROR Revision__ExistsAsMaster(String bsn, String version);

		ERROR Revision_EqualsSameVersion_AsAlreadyImportedFrom_(String bsn, String version, String url);

		ERROR Revision_OlderVersion_AsAlreadyImportedFrom_(String bsn, String version, URL url);

	}

	final ImporterMessages	messages	= ReporterMessages.base(this, ImporterMessages.class);
	String					owner;
	String					message;

	public LibraryImporterImpl(LibraryImpl parent, String url) {
		this.parent = parent;
		this.url = url;
	}

	public Revision fetch() throws Exception {
		String uniqueId = new URL(url).toExternalForm();
		RevisionImpl rev = parent.revisions.find("_id=%s", uniqueId).one();
		if (rev != null && rev.master) {
			messages.AlreadyImported_(uniqueId);
			return rev;
		}

		rev = new RevisionImpl();
		rev.url = new URI(url);
		rev.owner = owner;
		rev.message = message;

		for (MetadataProvider md : parent.mdps) {
			trace("parsing %s with %s", rev.url, md);
			Report report = md.parser(rev);
			getInfo(report);
		}

		verify(rev);

		if (!isOk()) {
			return null;
		}

		ProgramImpl program = parent.programs.find("_id=%s", rev.bsn).one();
		if (program != null) {
			RevisionRef ref = program.getRevision(rev.version);
			if (ref != null) {
				if (ref.master) {
					messages.Revision__ExistsAsMaster(rev.bsn, rev.version);
					return null;
				}
			}
		}

		rev._id = uniqueId;
		parent.revisions.upsert(rev);

		// Create a ref to the revision
		RevisionRef ref = new RevisionRef(rev);
		String version = new Version(rev.version).getWithoutQualifier().toString();

		if (program == null) {
			// Program does not exist yet
			program = new ProgramImpl();
			program._id = rev.bsn;
			data.assignIfNotSet(rev, program, "docUrl", "vendor", "description", "icon");
			program.revisions.add(ref);
			parent.programs.insert(program);
		} else {

			data.assignIfNotSet(rev, program, "docUrl", "vendor", "description", "icon");
			program.revisions.add(ref);
			int n = parent.programs.optimistic(program)
					.where("!(&(revisions.master=true)(revisions.version=%s))", version).append("revisions", ref)
					.update();

			if (n == 0) {
				messages.Revision__ExistsAsMaster(rev.bsn, rev.version);
				return null;
			}
			program.revisions.add(ref);

		}
		return rev;
	}

	/**
	 * Verify that all necessary fields are set, have the proper format etc.
	 * 
	 * @param rev
	 */
	private void verify(Revision rev) {
		check(rev.bsn, "bsn", Verifier.SYMBOLICNAME);
		check(rev.version, "version", Verifier.VERSION);
	}

	private void check(String field, String name, Pattern symbolicname) {

	}

	@Override
	public Importer owner(String email) {
		owner = email;
		return this;
	}

	@Override
	public Importer message(String message) {
		this.message = message;
		return this;
	}

}
