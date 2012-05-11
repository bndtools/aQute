package aQute.impl.cafs;

import java.io.*;

import junit.framework.*;
import aQute.bnd.annotation.component.*;
import aQute.impl.hazelcast.*;
import aQute.impl.store.mongo.*;
import aQute.lib.io.*;
import aQute.libg.cryptography.*;
import aQute.service.cafs.*;
import aQute.test.dummy.ds.*;
import aQute.test.dummy.log.*;

public class CAFSTest extends TestCase {
	DummyDS			ds	= new DummyDS();
	CAFSImpl			cafs;
	
	@Reference
	void setCAFS(CAFSImpl cafs) throws Exception {
		this.cafs = cafs;
		cafs.clearForTest();
	}

	public void setUp() throws Exception {
		ds.add(this);
		ds.add(MongoDBImpl.class).$("db", "test");
		ds.add(HazelcastImpl.class).$("multicast", false).$("name", "test");
		ds.add(new DummyLog().direct().stacktrace());
		ds.add(CAFSImpl.class);
		ds.wire();
	}

	public void testSimple() throws Exception {
		byte[] data = "Y".getBytes("UTF-8");
		Digester<SHA1> SHA1d = SHA1.getDigester();
		IO.copy(new ByteArrayInputStream(data), SHA1d);
		SHA1 sha1 = SHA1d.digest();
		System.out.println(sha1);

		byte[] digest = cafs.store(new ByteArrayInputStream(data));
		assertEquals(new SHA1(digest), sha1);

		InputStream in = cafs.retrieve(sha1.digest());
		String c = IO.collect(in);
		assertEquals("Y", c);

		cafs.store(new ByteArrayInputStream(data));

		CatalogBuilder cb = cafs.builder();
		cb.add("META-INF/MANIFEST.MF", new ByteArrayInputStream(
				"Manifest-Version: 1".getBytes()));
		Catalog cat = cb.build();
		assertNotNull(cat);
		CatalogEntry entry = cat.getEntry("META-INF/MANIFEST.MF");
		assertNotNull(entry);
		assertEquals("Manifest-Version: 1", IO.collect(entry.getContent()));

		// ZipFile zf = new ZipFile( "generated/aQute.libsync.core.jar");
		// Enumeration< ? extends ZipEntry> e = zf.entries();
		// while ( e.hasMoreElements() ) {
		// ZipEntry entry = e.nextElement();
		// if ( entry.isDirectory()) {
		// cb.setTime(entry.getName(), entry.getTime());
		// } else {
		// cb.add(entry.getName(), zf.getInputStream(entry));
		// }
		// }
		// Catalog cat = cb.build();
		// assertNotNull( cat.getEntry("META-INF/MANIFEST.MF"));
	}

}
