package aQute.service.library;

import aQute.service.library.Library.Revision;
import aQute.service.reporter.*;

public interface MetadataProvider {

	Report parser(Library.Importer importer, Revision revision) throws Exception;
}
