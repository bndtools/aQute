package aQute.service.blobstore;

import java.io.*;
import java.util.*;

public interface BlobStore {
	public interface BlobRead {
		BlobRead range(long begin, long end);

		BlobRead range(long begin);

		BlobRead ifModifiedSince(long time);

		BlobRead ifUnmodifiedSince(long time);

		BlobRead ifMatch(String etag);

		BlobRead ifNotMatch(String etag);

		InputStream asInputStream() throws Exception;

		Reader asReader() throws Exception;

		Reader asReader(String charset) throws Exception;

		File asFile() throws Exception;

		boolean to(File o) throws Exception;

		boolean to(OutputStream o) throws Exception;

		long getContentLength();

		String getContentType();

		String getEtag();

		long getLastModified();

		long getDate();
	}

	BlobRead read(String name);

	public interface BlobWrite {
		BlobWrite setContentLength(long size);

		BlobWrite setContentType(String type);

		void write(File file) throws Exception;

		void write(InputStream in) throws Exception;

	}

	BlobWrite write(String name);

	public interface ListRequest extends Iterable<String> {
		ListRequest limit(int limit);

		ListRequest start(String start);

		Iterator<String> iterator();
	}

	ListRequest list(String directory) throws Exception;

	void copy(String from, String to) throws Exception;

	void rename(String from, String to) throws Exception;

	void delete(String name) throws Exception;
}
