package aQute.impl.gitposthook;

import java.net.*;
import java.util.*;

import aQute.impl.gitposthook.Data.Delta;
import aQute.libg.reporter.*;
import aQute.service.github.*;
import aQute.service.library.*;
import aQute.service.library.Library.Revision;

public class Importer extends ReporterAdapter {
	final Data.Posthook	posthook;
	final Github		github;
	final Repository	repo;
	final List<URI>		files	= new ArrayList<URI>();
	final Library		library;

	public Importer(Github github, Library registry, Data.Posthook posthook) {
		this.posthook = posthook;
		this.github = github;
		this.library = registry;
		repo = github.getRepository(posthook.repository.name)
				.owner(posthook.repository.owner.name).get();
	}

	void scan() throws Exception {
		List<Branch> branches = repo.getBranches();
		for (Branch branch : branches) {
			if (branch.name.equals("master") || branch.name.equals("snapshot")) {
				Commit commit = repo.getCommit(branch.commit.sha);
				scan(commit);
				importAll(files, branch.name.equals("master"));
			} else
				trace("ignoring branch %s", branch.name);
		}
	}

	private void scan(Commit commit) throws Exception {
		Tree tree = repo.getTree(commit.tree.sha);
		collect(files, tree);
	}

	private void collect(List<URI> files, Tree tree) throws Exception {
		for (Entry entry : tree.tree) {
			if (entry.path.endsWith(".jar")) {
				URI blob = repo.getBlob(entry.sha);
				files.add(blob);
			}
			if (entry.type == Entry.Type.tree) {
				collect(files, repo.getTree(entry.sha));
			}
		}
	}

	private void importAll(List<URI> files, boolean master) throws Exception {
		for (URI uri : files) {
			try {
				Revision rev = library.insert(uri.toString());
				if (rev == null) {
					error("Could not insert %s", uri);
				} else if (master)
					; //library.master(rev);

			} catch (Exception e) {
				e.printStackTrace();
				error("Failed to insert %s : %s", uri, e);
			}
		}
	}

	void delta() throws Exception {

		// String branch = posthook.ref;
		// boolean master = branch.equals("refs/heads/master");

		String owner = posthook.repository.owner.name;
		String repo = posthook.repository.name;

		Repository r = github.getRepository(repo).owner(owner).get();

		for (Delta c : posthook.commits) {
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

}
