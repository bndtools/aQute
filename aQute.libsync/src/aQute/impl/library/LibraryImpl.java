package aQute.impl.library;

import java.net.*;
import java.util.*;
import java.util.concurrent.*;

import org.osgi.service.log.*;

import aQute.bnd.annotation.component.*;
import aQute.lib.converter.*;
import aQute.service.filecache.*;
import aQute.service.library.*;
import aQute.service.store.*;

@Component
public class LibraryImpl implements Library {
	WeakHashMap<String,ProgramImpl>	cache	= new WeakHashMap<String,LibraryImpl.ProgramImpl>();

	Store<ProgramImpl>				programs;
	Store<RevisionImpl>				revisions;
	LogService						log;
	FileCache						fileCache;
	List<MetadataProvider>			mdps	= new CopyOnWriteArrayList<MetadataProvider>();

	interface Config {
		boolean trace();

	}

	Config	config;

	@Activate
	void activate(Map<String,Object> p) throws Exception {
		this.config = Converter.cnv(Config.class, p);
	}

	static public class ProgramImpl extends Program {

		public boolean upsert(RevisionRef ref) {
			boolean upsert = revisions.remove(ref);
			revisions.add(ref);
			return upsert;
		}
	}

	static public class RevisionImpl extends Revision {

		// public static final String _type = "revision";

	}

	public Program getProgram(String bsn) throws Exception {
		Program p = cache.get(bsn);
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
		else
			cursor.limit(100);

		return cursor.select();
	}

	String stagingName(Revision rev) {
		return "staging/" + filename(rev);
	}

	String masterName(Revision rev) {
		return "master/" + filename(rev);
	}

	String filename(Revision rev) {
		return rev.bsn + "/" + rev.version.base + "/" + rev.bsn + "-" + rev.version.base + ".jar";
	}

	void unsetMetadataProviders(MetadataProvider mdp) {
		mdps.remove(mdp);
	}

	@Override
	public Revision getRevision(String bsn, String version) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Importer importer(URI url) throws Exception {
		return new LibraryImporterImpl(this, url);
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

	@Reference(type = '*')
	void setMetadataProviders(MetadataProvider mdp) {
		mdps.add(mdp);
	}

	@Reference
	void setFileCache(FileCache cache) {
		this.fileCache = cache;
	}
}
