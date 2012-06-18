package aQute.service.github;

import java.net.*;

public class Entry {
	public enum Type {
		blob, tree
	}

	public String	path;
	public long		mode;
	public Type		type;
	public long		size;
	public String	sha;
	public URI		url;
}
