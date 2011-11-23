package aQute.aws.s3;

import java.io.*;

import service.aws.s3.Bucket.PutRequest;
import service.aws.s3.S3.StorageClass;
import aQute.lib.base64.*;
import aQute.lib.io.*;
import aQute.libg.cryptography.*;

public class PutRequestImpl extends CommonRequestImpl<PutRequest> implements
		PutRequest {
	final BucketImpl	bucket;
	final String		key;
	long				length	= -1;

	PutRequestImpl(S3Impl parent, BucketImpl bucketImpl, String key) {
		super(parent);
		this.bucket = bucketImpl;
		this.key = key;
	}

	@Override
	public PutRequestImpl contentEncoding(String s) {
		header("Content-Encoding", s);
		return this;
	}

	@Override
	public PutRequestImpl expect(String s) {
		header("Expect", s);
		return this;
	}

	@Override
	public PutRequestImpl expires(long ms) {
		header("Expires", Long.toString(ms));
		return this;
	}

	@Override
	public PutRequestImpl storageClass(StorageClass storageClass) {
		header("x-amz-storage-class", storageClass.toString());
		return this;
	}

	@Override
	public void put(InputStream in) throws Exception {
		if ( length == -1 ) {
			File tmpfile = File.createTempFile("awss3", ".tmp");
			FileOutputStream out = new FileOutputStream(tmpfile);
			Digester<MD5> md5 = MD5.getDigester(out);
			IO.copy(in,md5);
			if ( !headers.containsKey("Content-MD5"))
				headers.put("Content-MD5", Base64.encodeBase64(md5.digest().digest()));
			headers.put("Content-Length", Long.toString(tmpfile.length()));
			in = new FileInputStream(tmpfile);
		}
		System.out.println(headers);
		parent.construct(S3Impl.METHOD.PUT, bucket.getName(), key, in, headers,
				null);
	}

	public PutRequest contentLength(long length) {
		this.length = length;
		return header("Content-Length", Long.toString(length));
	}

}
