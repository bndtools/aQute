package aQute.service.github;

import java.util.*;

public class Commit extends Reference {
	public User				author;
	public User				committer;
	public String			message;
	public Reference		tree;
	public List<Reference>	parents;
}
