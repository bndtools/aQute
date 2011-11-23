package aQute.aws.s3;

import java.io.*;
import java.util.*;

import service.aws.s3.Bucket.GetRequest;
import service.aws.s3.Bucket.Range;

/**
 * Implements a get request so that the caller can provide all the options
 * without being confronted with a huge API.
 * 
 */
public class GetRequestImpl extends CommonRequestImpl<GetRequest> implements
		GetRequest {
	final String	bucket;
	final String	key;
	List<Range>		ranges;
	boolean			accept304;
	boolean			accept412;

	public GetRequestImpl(S3Impl parent, String bucket, String key) {
		super(parent);
		this.bucket = bucket;
		this.key = key;
	}

	@Override
	public GetRequestImpl range(Range range) {
		if (ranges == null)
			ranges = new ArrayList<Range>();
		ranges.add(range);
		return this;
	}

	@Override
	public GetRequestImpl ranges(Collection<Range> ranges) {
		for (Range range : ranges) {
			range(range);
		}
		return this;
	}

	@Override
	public GetRequestImpl ifModfiedSince(Date date) {
		header("If-Modified-Since", date);
		accept304 = true;
		return this;
	}

	@Override
	public GetRequestImpl ifUnmodfiedSince(Date date) {
		header("If-Unmodified-Since", date);
		accept412 = true;
		return this;
	}

	@Override
	public GetRequestImpl ifMatch(String etag) {
		header("If-Match", etag);
		accept412 = true;
		return this;
	}

	@Override
	public GetRequestImpl ifNoneMatch(String etag) {
		header("If-None-Match", etag);
		accept304 = true;
		return this;
	}

	@Override
	public InputStream get() throws Exception {
		// check for ranges
		if (ranges != null) {
			StringBuilder sb = new StringBuilder();
			String del = "bytes=";
			for (Range range : ranges) {
				sb.append(del);
				sb.append(range.start);
				if (range.length > 0) {
					sb.append("-");
					sb.append(range.length - 1);
				}
				del = ",";
			}
			header("Range", sb.toString());
		}

		try {
			return parent.construct(S3Impl.METHOD.GET, bucket, key, null, headers, null);
		}
		catch (S3Exception e) {
			if (e.responseCode == 304 && accept304)
				return null;

			if (e.responseCode == 412 && accept412)
				return null;
			
			throw e;
		}
	}

}
