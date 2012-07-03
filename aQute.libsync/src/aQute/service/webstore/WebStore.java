package aQute.service.webstore;

import java.io.*;

public interface WebStore {
	String path(InputStream in) throws Exception;

	InputStream get(String path);
}
