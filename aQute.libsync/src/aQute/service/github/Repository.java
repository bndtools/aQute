package aQute.service.github;

import java.net.*;
import java.util.*;

public interface Repository {
	Commit getCommit(String sha) throws Exception;

	Tree getTree(String sha) throws Exception;

	URI getBlob(String sha) throws Exception;

	URI getBlob(Tree tree, String path) throws Exception;

	Entry getEntry(Tree tree, String path) throws Exception;

	List<Branch> getBranches() throws Exception;
}
