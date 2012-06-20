package service.records;

import java.io.*;

public interface Codec {
	<T> InputStream encode(T object) throws Exception;

	<T> T decode(Class<T> clazz) throws Exception;
}
