package aQute.service.library;

import java.net.*;
import java.util.*;

import aQute.service.reporter.*;

public interface Library {
	public enum PackageType {
		IMPORT, EXPORT, PRIVATE
	};

	public class Revision {
		public String				_id;												// Unique
																						// id
		public URI					url;
		public String				bsn;
		public String				version;
		public String				tag;
		public boolean				master;
		public long					insertDate;
		public URI					docUrl;
		public String				vendor;
		public String				description;
		public List<License>		licenses;
		public SCM					scm;
		public List<Developer>		developers;
		public List<Developer>		contributors;
		public Set<String>			category;
		public String				summary;
		public List<PackageDef>		packages	= new ArrayList<Library.PackageDef>();
		public Map<String,Object>	metadata	= new HashMap<String,Object>();
		public URI					icon;
		public String				message;
		public String				owner;												// email
		public Map<String,Object>	__extra;
	}

	public class PackageDef {
		public String		name;
		public String		version;	// range or version
		public PackageType	type;
		public Set<String>	uses;
	}

	public class Dependency {
		public String	bsn;
		public String	range;

	}

	public class License {
		public String				name;
		public String				description;
		public URI					link;
		public Map<String,Object>	__extra;
	}

	public class SCM {
		public String				connection;
		public String				developerConnection;
		public URI					url;
		public Map<String,Object>	__extra;
	}

	public class Developer {
		public String				id;
		public String				name;
		public String				email;
		public Map<String,Object>	__extra;
	}

	public class OrganizationRef {
		public String				organizationId;
		public String				name;
		public URI					url;
		public Map<String,Object>	__extra;
	}

	class RevisionRef {
		public RevisionRef() {}

		public RevisionRef(Revision revision) {
			this.revision = revision._id;
			this.bsn = revision.bsn;
			this.url = revision.url;
			this.master = revision.master;
			this.version = revision.version;
			this.summary = revision.summary;
			this.tag = revision.tag;
		}

		public URI		url;
		public String	bsn;
		public String	version;
		public String	revision;
		public String	tag;
		public boolean	master;
		public String	release;
		public String	summary;
	}

	class Program {
		// The _id must be the bsn (which is repeated)
		public String				_id;
		public String				mailingList;
		public String				scm;
		public List<RevisionRef>	revisions	= new ArrayList<Library.RevisionRef>();
		public URI					logo;
		public URI					docUrl;
		public String				vendor;
		public String				description;
		public URI					icon;
		public Map<String,Object>	__extra;
	}

	Program getProgram(String bsn) throws Exception;

	Iterable< ? extends Program> find(String where, int start, int limit) throws Exception;

	// void master(RevisionRef rev);

	Revision getRevision(String bsn, String version);

	interface Importer extends Report {
		Revision fetch() throws Exception;

		Importer owner(String email);

		Importer message(String msg);
	}

	Importer importer(String url) throws Exception;
}
