package aQute.service.github;

import java.net.*;

public interface Repository {
	Commit getCommit(String sha) throws Exception;
	Tree getTree(String sha) throws Exception;
	URL getBlob(String sha) throws Exception;
	URL getBlob(Tree tree, String path) throws Exception;
	Entry getEntry(Tree tree, String path) throws Exception;
}
