package aQute.impl.library;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.jar.*;

import org.osgi.service.log.*;

import aQute.bnd.annotation.component.*;
import aQute.lib.data.*;
import aQute.lib.osgi.*;
import aQute.libg.header.*;
import aQute.libg.reporter.*;
import aQute.libg.version.*;
import aQute.service.library.*;
import aQute.service.store.*;

@Component
public class LibraryImpl implements Library {
	WeakHashMap<String,ProgramImpl>	cache	= new WeakHashMap<String,LibraryImpl.ProgramImpl>();

	Store<ProgramImpl>				programs;
	Store<RevisionImpl>				revisions;
	LogService						log;

	static public class ProgramImpl extends Program {
		// public static final String _type = "program";

	}

	static public class RevisionImpl extends Revision {
		// public static final String _type = "revision";

	}

	/**
	 * Insert a JAR into the registry
	 * 
	 * @param url
	 * @throws Exception
	 */
	public Revision insert(String url) throws Exception {
		ReporterAdapter reporter = new ReporterAdapter();
		RevisionImpl revision = parse(url, reporter);

		if (!reporter.isOk())
			throw new Exception(reporter.toString());

		revision._id = revisions.uniqueId();
		ProgramImpl program = programs.all().where("_id=%s", revision.bsn).one();

		if (program != null) {
			RevisionRef ref = getRevision(program, revision.version);
			if (ref != null && ref.master)
				throw new Exception("100 Attempt to insert a master revision " + revision.bsn + "-" + revision.version);
		}

		revisions.insert(revision); // Save, might become orphaned

		try {
			// Create a ref to the revision
			RevisionRef ref = new RevisionRef(revision);

			if (program == null) {
				program = new ProgramImpl();
				data.assignIfNotSet(revision, program, "bsn", "docUrl", "vendor", "description");
				program._id = revision.bsn;
				program.revisions.add(ref);
				programs.insert(program);
			} else {
				int n = programs.optimistic(program)
						.where("!(&(revisions.master=true)(revisions.version=%s))", revision.version)
						.append("revisions", ref).update();

				if (n == 0)
					throw new Exception("100 Attempt to insert a master revision " + revision.bsn + "-"
							+ revision.version);

				program.revisions.add(ref);
			}

		}
		catch (Exception t) {
			revisions.find(revision).remove();
			throw t;
		}
		return revision;
	}

	public Program getProgram(String bsn) throws Exception {
		ProgramImpl p = cache.get(bsn);
		if (p == null) {
			p = programs.find("_id=%s", bsn).one();
			// cache.put(bsn, p);
		}
		return p;
	}

	public Iterable< ? extends Program> find(String where, int skip, int limit) throws Exception {
		if (where == null)
			where = "(bsn=*)";

		Cursor<ProgramImpl> cursor = programs.find(where).skip(skip);
		if (limit > 0)
			cursor.limit(limit);

		return cursor.select();
	}

	private RevisionImpl parse(String spec, Reporter reporter) throws IOException {
		try {
			URL url = new URL(spec);
			JarInputStream in = new JarInputStream(url.openStream());
			try {
				Manifest m = in.getManifest();
				if (m == null) {
					reporter.error("No manifest in bundle", spec);
					return null;
				}
				Attributes attributes = m.getMainAttributes();
				if (attributes.size() < 3) {
					reporter.error("Manifest has no meta data", spec);
					return null;
				}
				Domain domain = Domain.domain(attributes);

				RevisionImpl revision = new RevisionImpl();
				revision.bsn = domain.getBundleSymbolicName();

				String v = domain.getBundleVersion();
				if (!Verifier.isVersion(v)) {
					reporter.error("Invalid version %s", v);
					v = "0";
				}
				revision.version = new Version(v).getWithoutQualifier().toString();
				revision.qualifier = new Version(v).getQualifier();
				revision.insertDate = System.currentTimeMillis();
				revision.description = domain.get(Constants.BUNDLE_DESCRIPTION);
				revision.vendor = domain.get(Constants.BUNDLE_VENDOR);
				revision.docUrl = domain.get(Constants.BUNDLE_DOCURL);
				revision.url = spec;

				Parameters licenses = OSGiHeader.parseHeader(domain.get(Constants.BUNDLE_LICENSE));
				if (licenses.size() > 0) {
					revision.licenses = new ArrayList<License>();
					for (java.util.Map.Entry<String,Attrs> x : licenses.entrySet()) {
						Attrs attrs = x.getValue();
						License license = new License();
						license.name = x.getKey();
						license.link = attrs.get("link");
						license.description = attrs.get("description");
						revision.licenses.add(license);
					}
				}

				revision.jpmCommand = attributes.getValue("JPM-Command");
				revision.jpmService = attributes.getValue("JPM-Service");

				return revision;
			}
			finally {
				in.close();
			}
		}
		catch (Exception e) {
			reporter.error("Errors during opening of URL", spec);
		}
		return null;
	}

	public void master(RevisionRef rev) {
		// TODO Auto-generated method stub

	}

	public Revision getRevision(RevisionRef ref) {
		// TODO Auto-generated method stub
		return null;
	}

	@Reference
	void setStore(DB db) throws Exception {
		programs = db.getStore(ProgramImpl.class, "library.program");
		revisions = db.getStore(RevisionImpl.class, "library.revision");
	}

	@Reference
	void setLog(LogService log) throws Exception {
		this.log = log;
	}

	public RevisionRef getRevision(Program program, String version) {
		// TODO with or without?
		for (RevisionRef ref : program.revisions)
			if (version.equals(ref.version))
				return ref;

		return null;
	}
}
