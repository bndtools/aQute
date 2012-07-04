package aQute.service.filecache;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public interface FileCache {

	File get(String name, Callable<InputStream> cb) throws Exception;

	File get(String name, URI url) throws Exception;

	long getExpiration(File file);

	long setExpiration(File file, long expiration);
}
