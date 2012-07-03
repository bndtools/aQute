package aQute.impl.s3.blobstore;

import java.io.*;
import java.util.*;

import junit.framework.*;
import aQute.impl.blobstore.s3.*;
import aQute.lib.io.*;
import aQute.service.blobstore.*;
import aQute.test.dummy.ds.*;
import aQute.test.dummy.log.*;

public class BlobStoreTest extends TestCase {
	DummyDS		ds			= new DummyDS();
	Properties	properties	= new Properties();

	public void setUp() throws Exception {
		properties.load(new FileInputStream(IO.getFile(System.getProperty("user.home") + "/.bnd/local.properties")));
		ds.add(S3BlobStoreImpl.class).$(".awsId", properties.get(".awsId"))
				.$(".awsSecret", properties.get(".awsSecret")).$("bucket", "BlobStoreTest")
				.$("storageClass", "REDUCED");
		ds.add(new DummyLog().direct().stacktrace().full());
		ds.wire();
	}

	public void testSimple() throws Exception {
		BlobStore bs = ds.get(BlobStore.class);
		bs.delete("test");
		bs.write("test").write(new ByteArrayInputStream("Hello World".getBytes("UTF-8")));
		for (String key : bs.list("")) {
			assertEquals("test", key);
		}
		String s = IO.collect(bs.read("test").asInputStream());
		assertEquals("Hello World", s);
		bs.delete("test");

	}
}
