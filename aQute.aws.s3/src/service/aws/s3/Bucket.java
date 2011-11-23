package service.aws.s3;

import java.io.*;
import java.util.*;

import service.aws.s3.S3.*;

public interface Bucket {

	public class Content {
		public Bucket		bucket;
		public String		key;
		public Date			lastModified;
		public long			size;
		public StorageClass	storageClass;
		public String		etag;
	}

	public class Range {
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

	String getName();

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

	GetRequest getObject(String key) throws Exception;

	public interface PutRequest extends CommonRequest<PutRequest> {
		PutRequest contentEncoding(String s);

		PutRequest expect(String s);

		PutRequest expires(long ms);

		PutRequest storageClass(S3.StorageClass storageClass);

		PutRequest contentLength(long length);

		void put(InputStream in) throws Exception;
	}

	PutRequest putObject(String key) throws Exception;

	public interface ListRequest extends CommonRequest<ListRequest>,
			Iterable<Content> {
		ListRequest delimeter(String delimeter);

		ListRequest marker(String marker);

		ListRequest maxKeys(int maxKeys);

		ListRequest prefix(String prefix);
	}

	ListRequest listObjects() throws Exception;

	void delete(String key) throws Exception;
}
