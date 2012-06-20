package test;

import junit.framework.*;
import aQute.aws.credentials.*;
import aQute.aws.s3.*;
import aQute.aws.s3.Bucket.Content;
import aQute.lib.io.*;

public class S3Test extends TestCase {

	public void testSimple() throws Exception {
		UserCredentials uc = new UserCredentials();

		S3 s3 = new S3(uc.getAWSAccessKeyId(), uc.getAWSSecretKey());
		deleteBucket(s3);

		Bucket bucket = s3.createBucket("libsync-test");
		try {
			assertNotNull(bucket);
			assertEquals("libsync-test", bucket.getName());
			assertFalse(bucket.listObjects().iterator().hasNext());

			boolean found = false;
			for (Bucket b : s3.listBuckets()) {
				if ("libsync-test".equals(b.getName()))
					found = true;
			}
			assertTrue(found);

			bucket.putObject("1/a").put("0123");
			bucket.putObject("2/b").put("abcdefgh");

			boolean one = false;
			boolean two = true;
			int count = 0;
			for (Content content : bucket.listObjects()) {
				count++;
				assertEquals(bucket, content.bucket);
				if ("1/a".equals(content.key)) {
					one = true;
					assertEquals("eb62f6b9306db575c2d596b1279627a4", content.etag);
					assertEquals(4, content.size);
					String c = IO.collect(bucket.getObject("1/a").get());
					assertEquals("0123", c);
				} else if ("2/b".equals(content.key)) {
					one = true;
					assertEquals("e8dc4081b13434b45189a720b77b6818", content.etag);
					assertEquals(8, content.size);
					String c = IO.collect(bucket.getObject("2/b").get());
					assertEquals("abcdefgh", c);
				} else
					fail("Unrecognized content " + content.key);
			}
			assertEquals(2, count);
			assertTrue(one);
			assertTrue(two);

			bucket.putObject("1/a").put("aa");
			bucket.delete("hullo");
			bucket.delete("bye");
		}
		finally {
			deleteBucket(s3);
		}
	}

	private void deleteBucket(S3 s3) {
		try {
			Bucket b = s3.getBucket("libsync-test");
			if (b != null) {
				for (Content c : b.listObjects()) {
					b.delete(c.key);
				}
				s3.deleteBucket("libsync-test");
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
