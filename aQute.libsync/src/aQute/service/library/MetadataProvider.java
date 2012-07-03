package aQute.service.library;

import aQute.service.library.Library.Revision;
import aQute.service.reporter.*;

public interface MetadataProvider {

	Report parser(Revision partialRevision) throws Exception;
}
