package aQute.service.cafs;

import java.io.*;

public interface CAFS {
	CatalogBuilder builder() throws Exception;

	Catalog getCatalog(byte[] sha) throws Exception;

	InputStream retrieve(byte[] sha) throws Exception;

	byte[] store(InputStream in) throws Exception;
}
