package aQute.impl.github;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.net.ssl.*;

import aQute.lib.base64.*;
import aQute.lib.collections.*;
import aQute.lib.io.*;
import aQute.lib.json.*;
import aQute.service.github.*;

public class RepositoryImpl implements Repository {
	final static String		BASE_URL	= "https://api.github.com/repos/";
	final static JSONCodec	codec		= new JSONCodec();
	final String			owner;
	final String			repo;
	final String			baseurl;
	final GithubImpl		github;
	final String			secret;
	final String			user;

	public RepositoryImpl(GithubImpl github, String owner, String name, String user, String secret) {
		this.owner = owner;
		this.repo = name;
		this.baseurl = BASE_URL + owner + "/" + repo + "/";
		this.github = github;
		this.user = user;
		this.secret = secret;
	}

	public Commit getCommit(String sha) throws Exception {

		return read(Commit.class, "git/commits/" + sha);
	}

	public Tree getTree(String sha) throws Exception {
		return read(Tree.class, "git/trees/" + sha);
	}

	public InputStream getBlob(final String sha) throws Exception {
		URL url = new URL(baseurl + "git/blobs/" + sha);
		HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
		// conn.setRequestProperty("Accept",
		// "application/vnd.github.VERSION.raw");
		conn.addRequestProperty("Accept", "application/vnd.github.raw");
		return conn.getInputStream();
	}

	private <T> T read(Class<T> clazz, String string) throws Exception {
		URL url = new URL(baseurl + string);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();

		// Do we need to authenticate?
		if (github.config._secret() != null && github.config.user() != null) {
			String auth = github.config.user() + ":" + github.config._secret();
			conn.setRequestProperty("Authorization", "Basic " + Base64.encodeBase64(auth.getBytes("UTF-8")));
		}
		int code = conn.getResponseCode();
		if (code != 200) {
			return null;
		}
		InputStream in = conn.getInputStream();
		try {
			String s = IO.collect(in);
			return codec.dec().from(s).get(clazz);
		}
		finally {
			in.close();
		}
	}

	public InputStream getBlob(Tree root, String path) throws Exception {
		String parts[] = path.split("/");
		Entry e = find(root, parts, 0);
		if (e == null)
			return null;

		return getBlob(e.sha);
	}

	private Entry find(Tree root, String[] parts, int i) throws Exception {
		if (i >= parts.length)
			return null;

		String target = parts[i];
		for (Entry entry : root.tree) {
			if (target.equals(entry.path)) {
				// Are we at the end?
				if (i == parts.length - 1)
					return entry;

				// Is it a tree?
				if (entry.type == Entry.Type.tree) {
					Tree next = getTree(entry.sha);
					if (next == null) {
						assert true;
						// TODO a bit bizarre
						// a tree must always exist
						return null;
					}
					return find(next, parts, i + 1);
				}
				// file does not exist since we
				// have more parts but no tree
				return null;
			}
		}
		// Not found
		return null;
	}

	public Entry getEntry(Tree root, String path) throws Exception {
		String parts[] = path.split("/");
		return find(root, parts, 0);
	}

	public List<Branch> getBranches() throws Exception {
		Branch[] read = read(Branch[].class, "branches");
		if (read == null)
			return null;

		return new SortedList<Branch>(read);
	}

	@Override
	public URI getURI(Commit commit, String path) throws URISyntaxException {
		return new URI("https://github.com/" + owner + "/" + repo + "/blob/" + commit.sha + path + "?raw=true");
	}
}
