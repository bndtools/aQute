package service.keyvaluedb;

import java.io.*;

public interface Blob {
	InputStream getInputStream() throws IOException;

	OutputStream getOutputStream(boolean append) throws IOException;
}
