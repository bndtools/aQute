package aQute.service.filecache;

import java.io.*;
import java.util.concurrent.*;

public interface FileCache {

	File get(String name, Callable<InputStream> cb) throws Exception;

	long getExpiration(File file);

	long setExpiration(File file, long expiration);
}
