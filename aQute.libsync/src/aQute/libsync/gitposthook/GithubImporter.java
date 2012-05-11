package aQute.libsync.gitposthook;

import java.net.*;
import java.util.*;

import aQute.bnd.annotation.component.*;
import aQute.bnd.annotation.component.Reference;
import aQute.libsync.gitposthook.Data.Delta;
import aQute.libsync.gitposthook.Data.Import;
import aQute.service.github.*;
import aQute.service.task.*;

@Component
public class GithubImporter implements Worker<Data.Import> {
	// ps Library library;
	Github	github;

	public void execute(Import work) throws Exception {
		String owner = work.posthook.repository.owner.name;
		String repo = work.posthook.repository.name;

		System.out.println("Github hook");
		List<URL> files = new ArrayList<URL>();

		Repository r = github.getRepository(repo).owner(owner).get();

		for (Delta c : work.posthook.commits) {
			Commit commit = r.getCommit(c.id);
			Tree tree = r.getTree(commit.tree.sha);

			System.out.println(c.url);
			for (String f : c.added) {
				files.add(r.getBlob(tree, f));
			}
			for (String f : c.modified) {
				files.add(r.getBlob(tree, f));
			}
		}
		System.out.println("Changed: " + files);

	}

	// @Reference
	// void setLibrary( Library lib) {
	// this.library = lib;
	// }

	@Reference
	void setGithub(Github github) {
		this.github = github;
	}
}
