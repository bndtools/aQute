package aQute.impl.metadata.osgi;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

import aQute.bnd.annotation.component.*;
import aQute.service.filecache.*;
import aQute.service.library.Library.Revision;
import aQute.service.library.*;
import aQute.service.reporter.*;

/**
 * 
 */
@Component
public class OSGiMetadataProvider implements MetadataProvider {
	FileCache	fileCache;

	public Report parser(Revision revision) throws Exception {
		final URL url = revision.url.toURL();

		File file = fileCache.get(url.toString(), new Callable<InputStream>() {
			public InputStream call() throws Exception {
				return url.openStream();
			}
		});

		OSGiMetadataParser parser = new OSGiMetadataParser(file, revision);
		parser.run();
		return parser;
	}

	@Reference
	void setFileCache(FileCache cache) {
		this.fileCache = cache;
	}
}
