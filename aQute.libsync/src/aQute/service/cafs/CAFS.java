package aQute.service.cafs;

import java.io.*;
import java.util.concurrent.*;

public interface CAFS {
	CatalogBuilder builder() throws Exception;

	Catalog getCatalog(byte[] sha) throws Exception;

	InputStream retrieve(byte[] sha) throws Exception;

	byte[] store(InputStream in) throws Exception;

	boolean store(byte[] sha, Callable<InputStream> in) throws Exception;
}
