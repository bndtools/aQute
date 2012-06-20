package aQute.service.cafs;

import java.util.*;

public interface Catalog extends Iterable<CatalogEntry> {
	CatalogEntry getEntry(String path) throws Exception;

	Map<String,CatalogEntry> getEntries() throws Exception;

	byte[] getId();
}
