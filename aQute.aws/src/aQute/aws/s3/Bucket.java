package aQute.aws.s3;

import java.io.*;
import java.util.*;

import aQute.aws.s3.S3.StorageClass;

public class Bucket {
	final String	name;
	final S3	parent;

	// TODO verify bucket name

	Bucket(S3 parent, String name) {
		this.parent = parent;
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public GetRequest getObject(String key) {
		return new GetRequestImpl(parent, name, key);
	}

	public void delete(String key) throws Exception {
		parent.construct(S3.METHOD.DELETE, name, key, null, null, null);
	}

	public PutRequest putObject(String key) {
		return new PutRequestImpl(parent,this,key);
	}

	public ListRequest listObjects() throws Exception {
		return new ListRequestImpl(parent, this);
	}

	public String toString() {
		return name;
	}

	public static class Content {
		public Bucket		bucket;
		public String		key;
		public Date			lastModified;
		public long			size;
		public StorageClass	storageClass;
		public String		etag;		
	}

	public static class Range {
		final public long	start;
		final public long	length;

		public Range(long start, long length) {
			assert length > 0;
			this.start = start;
			this.length = length;
		}

		public Range(long start) {
			this.start = start;
			this.length = -1;
		}
	}

	public interface CommonRequest<T> {
		T contentType(String string);

		T date(Date date);

		T contentMD5(String etag);

		T header(String header, String value);

		T argument(String name, String value);
	}

	public interface GetRequest extends CommonRequest<GetRequest> {
		GetRequest range(Range range);

		GetRequest ranges(Collection<Range> range);

		GetRequest ifModfiedSince(Date date);

		GetRequest ifUnmodfiedSince(Date date);

		GetRequest ifMatch(String etag);

		GetRequest ifNoneMatch(String etag);

		GetRequest contentType(String string);

		InputStream get() throws Exception;
	}


	public interface PutRequest extends CommonRequest<PutRequest> {
		PutRequest contentEncoding(String s);

		PutRequest expect(String s);

		PutRequest expires(long ms);

		PutRequest storageClass(S3.StorageClass storageClass);

		PutRequest contentLength(long length);

		void put(InputStream in) throws Exception;

		/**
		 * Put a string as UTF-8 encoded data.
		 * 
		 * @param in The string to put
		 * @throws Exception
		 */
		void put(String in) throws Exception;
	}


	public interface ListRequest extends CommonRequest<ListRequest>,
			Iterable<Content> {
		ListRequest delimeter(String delimeter);

		ListRequest marker(String marker);

		ListRequest maxKeys(int maxKeys);

		ListRequest prefix(String prefix);
	}

}
