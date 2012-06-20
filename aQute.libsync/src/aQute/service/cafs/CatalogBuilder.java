package aQute.service.cafs;

import java.io.*;

public interface CatalogBuilder {
	Catalog build() throws Exception;

	CatalogBuilder add(String path, InputStream in) throws Exception;

	CatalogBuilder setTime(String path, long time) throws Exception;

	CatalogBuilder setComment(String path, String comment) throws Exception;

	void close();
}
