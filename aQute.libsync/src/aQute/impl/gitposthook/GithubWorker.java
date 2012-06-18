package aQute.impl.gitposthook;

import javax.servlet.*;

import org.osgi.service.http.*;
import org.osgi.service.log.*;

import aQute.bnd.annotation.component.*;
import aQute.bnd.annotation.component.Reference;
import aQute.data.*;
import aQute.impl.gitposthook.Data.Import;
import aQute.lib.base64.*;
import aQute.service.github.*;
import aQute.service.library.*;
import aQute.service.store.*;
import aQute.service.task.*;

@Component(provide={Worker.class, GithubWorker.class})
public class GithubWorker implements Worker<Data.Import> {

	public static class Receipt extends Id {
		public Receipt() {}
		
		Receipt(String sha) {
			_id = Base64.decodeBase64(sha);
		}

		public String	repository;
		public String	url;
		public long		time;
	}

	Github			github;
	Store<Receipt>	receipts;
	LogService		log;
	Library			library;

	public void execute(Import work) throws Exception {
		Receipt after = new Receipt(work.posthook.after);
		Receipt before = new Receipt(work.posthook.before);

		Receipt r = receipts.find(after).select().one();

		if (r != null) {
			// We already have processed this hook
			log.log(LogService.LOG_INFO, "Alread up to date for repo "
					+ work.posthook.repository.name);
			return;
		}

		r = receipts.find(before).select().one();

		Importer importer = new Importer(github, library, work.posthook);
		if ( r == null )
			importer.scan();
		else
			importer.delta();

		if (!importer.isOk()) {
			log.log(LogService.LOG_ERROR,
					"failed to execute Github import hook for commit "
							+ work.posthook.after);
			return;
		}

		after.repository = work.posthook.repository.name;
		after.time = System.currentTimeMillis();
		after.url = work.posthook.repository.url;
		receipts.insert(after);
	}

	@Reference
	void setGithub(Github github) {
		this.github = github;
	}

	@Reference
	void setDb(DB db) throws Exception {
		this.receipts = db.getStore(Receipt.class, "library.receipt");
	}
	@Reference
	void setLibrary(Library lib) throws Exception {
		this.library = lib;
	}
	
	@Reference
	void setLog( LogService log) throws ServletException, NamespaceException {
		this.log = log;
	}

}
