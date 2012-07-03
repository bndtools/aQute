package aQute.impl.blobstore.s3;

import java.io.*;
import java.util.*;

import org.osgi.service.log.*;

import aQute.aws.s3.*;
import aQute.aws.s3.Bucket.Content;
import aQute.aws.s3.Bucket.GetRequest;
import aQute.aws.s3.Bucket.PutRequest;
import aQute.aws.s3.Bucket.Range;
import aQute.bnd.annotation.component.*;
import aQute.impl.blobstore.s3.S3BlobStoreImpl.Config;
import aQute.lib.converter.*;
import aQute.lib.io.*;
import aQute.service.blobstore.*;

@Component(designateFactory = Config.class)
public class S3BlobStoreImpl implements BlobStore {
	S3			s3;
	Bucket		bucket;
	LogService	log;

	interface Config {
		enum StorageClass {
			REDUCED, STANDARD
		}

		String _awsSecret();

		String _awsId();

		String bucket();

		String[] region();

		StorageClass storageClass();
	}

	Config	config;

	@Activate
	void activate(Map<String,Object> props) throws Exception {
		config = Converter.cnv(Config.class, props);
		s3 = new S3(config._awsId(), config._awsSecret());

		bucket = s3.getBucket(config.bucket());
	}

	@Override
	public BlobRead read(String name) {
		final GetRequest request = bucket.getObject(name);

		return new BlobRead() {
			@Override
			public BlobRead range(long begin, long end) {
				request.range(new Range(begin, end));
				return this;
			}

			@Override
			public BlobRead range(long begin) {
				request.range(new Range(begin));
				return this;
			}

			@Override
			public BlobRead ifModifiedSince(long time) {
				request.ifModfiedSince(new Date(time));
				return this;
			}

			@Override
			public BlobRead ifUnmodifiedSince(long time) {
				request.ifUnmodfiedSince(new Date(time));
				return this;
			}

			@Override
			public BlobRead ifMatch(String etag) {
				request.ifMatch(etag);
				return this;
			}

			@Override
			public BlobRead ifNotMatch(String etag) {
				request.ifNoneMatch(etag);
				return this;
			}

			@Override
			public InputStream asInputStream() throws Exception {
				return request.get();
			}

			@Override
			public Reader asReader() throws Exception {
				return IO.reader(asInputStream());
			}

			@Override
			public Reader asReader(String charset) throws Exception {
				return IO.reader(asInputStream(), charset);
			}

			@Override
			public File asFile() throws Exception {
				File f = File.createTempFile("s3", ".unknown");
				if (to(f))
					return f;
				f.delete();
				return null;
			}

			@Override
			public boolean to(File o) throws Exception {
				IO.copy(asInputStream(), o);
				return false;
			}

			@Override
			public boolean to(OutputStream o) throws Exception {
				InputStream in = asInputStream();
				if (in == null)
					return false;

				IO.copy(in, o);
				return true;
			}

			@Override
			public long getContentLength() {
				throw new UnsupportedOperationException();
			}

			@Override
			public String getContentType() {
				throw new UnsupportedOperationException();
			}

			@Override
			public String getEtag() {
				throw new UnsupportedOperationException();
			}

			@Override
			public long getLastModified() {
				throw new UnsupportedOperationException();
			}

			@Override
			public long getDate() {
				throw new UnsupportedOperationException();
			}

		};
	}

	@Override
	public BlobWrite write(String name) {
		final PutRequest request = bucket.putObject(name);
		return new BlobWrite() {

			@Override
			public BlobWrite setContentLength(long size) {
				request.contentLength(size);
				return this;
			}

			@Override
			public BlobWrite setContentType(String type) {
				request.contentType(type);
				return this;
			}

			@Override
			public void write(File file) throws Exception {
				write(new FileInputStream(file));
			}

			@Override
			public void write(InputStream in) throws Exception {
				request.put(in);
			}

		};
	}

	@Override
	public ListRequest list(String directory) throws Exception {
		final aQute.aws.s3.Bucket.ListRequest request = bucket.listObjects().delimeter("/").prefix(directory);
		return new ListRequest() {

			@Override
			public ListRequest start(String start) {
				request.marker(start);
				return this;
			}

			@Override
			public ListRequest limit(int limit) {
				request.maxKeys(limit);
				return this;
			}

			@Override
			public Iterator<String> iterator() {
				final Iterator<Content> it = request.iterator();
				return new Iterator<String>() {

					@Override
					public boolean hasNext() {
						return it.hasNext();
					}

					@Override
					public String next() {
						return it.next().key;
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}

				};
			}
		};

	}

	@Override
	public void copy(String from, String to) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void rename(String from, String to) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void delete(String name) throws Exception {
		bucket.delete(name);

	}

	@Reference
	void setLogService(LogService log) {
		this.log = log;
	}
}
