package aQute.service.cafs;

import java.io.*;

public interface CatalogEntry {
	boolean isCatalog();

	Catalog getCatalog() throws Exception;

	InputStream getContent() throws Exception;

	String getName();

	String getComment();

	int getSize();

	long getTime();

	byte[] getDigest();
}
