package aQute.service.library;

import java.util.*;

public interface Library {
	public class Meta {
		public String	bsn;
		public String	docUrl;
		public String	vendor;
		public String	description;
	}

	public class Revision extends Meta {
		public byte[]				_id;
		public String				version;
		public String				url;
		public String				jpmCommand;
		public String				jpmService;
		public String				qualifier;
		public boolean				master;
		public long					insertDate;
		public ArrayList<License>	licenses;
	}

	public class License {
		public String	name;
		public String	description;
		public String	link;
	}

	public class RevisionRef {
		public RevisionRef() {}

		public RevisionRef(Revision revision) {
			this.revision = revision._id;
			this.bsn = revision.bsn;
			this.url = revision.url;
			this.master = revision.master;
			this.version = revision.version;
			this.qualifier = revision.qualifier;
		}

		public byte[]	revision;
		public String	version;
		public String	bsn;
		public String	url;
		public boolean	master;
		public String	qualifier;
	}

	public class Program extends Meta {
		// The _id must be the bsn (which is repated)
		public String				_id;
		public String				mailingList;
		public String				scm;
		public List<RevisionRef>	revisions	= new ArrayList<RevisionRef>();

	}

	Revision insert(String url) throws Exception;

	Program getProgram(String bsn) throws Exception;

	Iterable< ? extends Program> find(String where) throws Exception;

	void master(RevisionRef rev);

	Revision getRevision(RevisionRef ref);
}
