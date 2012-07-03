package aQute.impl.gitposthook;

import java.util.*;

import aQute.data.common.*;

public class Data {
	public static class ImportData {
		public String	ip;
		public String	user;
		public long		time;
		public PosthookData	posthook;
	}

	public static class PosthookData {
		public Map<String, ? >	__extra;
		public String			before;
		public String			after;
		public String			ref;
		public List<Delta>		commits;
		public Repository		repository;
	}

	public static class Delta {
		public String			id;
		public String			message;
		public String			timestamp;
		public String			url;
		public List<String>		added;
		public List<String>		removed;
		public List<String>		modified;
		public Committer		author;
		public Map<String, ? >	__extra;
	}

	public static class Committer extends EmailStruct {
		public String	username;
	}

	public static class Repository {
		public String			name;
		public String			url;
		public String			pledgie;
		public String			description;
		public String			homepage;
		public int				watchers;
		public int				forks;
		public EmailStruct		owner;
		public Map<String, ? >	__extra;
	}

}
