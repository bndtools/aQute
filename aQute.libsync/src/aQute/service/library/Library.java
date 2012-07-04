package aQute.service.library;

import java.io.*;
import java.net.*;
import java.util.*;

import aQute.service.reporter.*;

public interface Library {
	public enum PackageType {
		IMPORT, EXPORT, PRIVATE
	};

	class Version {
		public String	base;
		public String	qualifier;
		public String	classifier;
		public String	original;

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((base == null) ? 0 : base.hashCode());
			result = prime * result + ((classifier == null) ? 0 : classifier.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Version other = (Version) obj;
			if (base == null) {
				if (other.base != null)
					return false;
			} else if (!base.equals(other.base))
				return false;
			if (classifier == null) {
				if (other.classifier != null)
					return false;
			} else if (!classifier.equals(other.classifier))
				return false;
			return true;
		}
	}

	public class Revision {
		public String				_id;												// Unique
		public byte[]				previous;											// id
		public URI					url;
		public String				bsn;
		public Version				version;
		public boolean				updated;
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
			this.updated = revision.updated;
		}

		public URI		url;
		public String	bsn;
		public Version	version;
		public String	revision;
		public String	tag;
		public boolean	master;
		public String	release;
		public String	summary;
		public boolean	updated;

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((bsn == null) ? 0 : bsn.hashCode());
			result = prime * result + ((version == null) ? 0 : version.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			RevisionRef other = (RevisionRef) obj;
			if (bsn == null) {
				if (other.bsn != null)
					return false;
			} else if (!bsn.equals(other.bsn))
				return false;
			if (version == null) {
				if (other.version != null)
					return false;
			} else if (!version.equals(other.version))
				return false;
			return true;
		}
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
		public String				lastImport;
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

		File getFile() throws Exception;

		URI getURL();
	}

	Importer importer(URI url) throws Exception;
}
