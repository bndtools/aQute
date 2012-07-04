package aQute.impl.metadata.osgi;

import aQute.bnd.annotation.component.*;
import aQute.service.filecache.*;
import aQute.service.library.Library.Importer;
import aQute.service.library.Library.Revision;
import aQute.service.library.*;
import aQute.service.reporter.*;

/**
 * 
 */
@Component
public class OSGiMetadataProvider implements MetadataProvider {
	FileCache	fileCache;

	@Reference
	void setFileCache(FileCache cache) {
		this.fileCache = cache;
	}

	@Override
	public Report parser(Importer importer, Revision revision) throws Exception {
		OSGiMetadataParser parser = new OSGiMetadataParser(importer.getFile(), revision);
		parser.run();
		return parser;
	}
}
