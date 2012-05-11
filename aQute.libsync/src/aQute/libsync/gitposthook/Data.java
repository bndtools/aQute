package aQute.libsync.gitposthook;

import java.net.*;
import java.util.*;

import aQute.data.common.*;

public class Data {
	public static class Import {
		public String			ip;
		public String			user;
		public long				time;
		public GithubPosthook	posthook;
	}

	public static class GithubPosthook {
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
		public URL				url;
		public List<String>		added;
		public List<String>		removed;
		public List<String>		modified;
		public Committer		author;
		public Map<String, ? >	__extra;
	}

	public static class Committer extends Email {
		public String	username;
	}

	public static class Repository {
		public String			name;
		public URL				url;
		public String			pledgie;
		public String			description;
		public URL				homepage;
		public int				watchers;
		public int				forks;
		public Email			owner;
		public Map<String, ? >	__extra;
	}

}
