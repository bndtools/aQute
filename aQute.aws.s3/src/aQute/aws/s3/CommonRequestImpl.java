package aQute.aws.s3;

import java.util.*;

import service.aws.s3.Bucket.CommonRequest;

@SuppressWarnings("unchecked")
public class CommonRequestImpl<T> implements CommonRequest<T> {
	final SortedMap<String, String>	headers	= new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
	final SortedMap<String, String>	arguments	= new TreeMap<String, String>();
	final S3Impl					parent;

	CommonRequestImpl(S3Impl parent) {
		this.parent = parent;
	}

	public T contentType(String value) {
		return header("Content-Type", value);
	}

	public T date(Date date) {
		return header("Date", parent.httpDate(date));
	}

	public T contentMD5(String etag) {
		return header("Content-MD5", etag);
	}

	public T header(String header, String value) {
		headers.put(header, value);
		return (T) this;
	}
	
	T header(String header, Date value) {
		headers.put(header, parent.httpDate(value));
		return (T) this;
	}

	@Override
	public T argument(String name, String value) {
		arguments.put(name, value);
		return (T) this;
	}
}
