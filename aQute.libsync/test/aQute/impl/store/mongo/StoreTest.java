package aQute.impl.store.mongo;

import java.io.*;
import java.util.*;

import junit.framework.*;
import aQute.bnd.annotation.component.*;
import aQute.lib.io.*;
import aQute.libg.version.*;
import aQute.test.dummy.ds.*;
import aQute.test.dummy.log.*;

public class StoreTest extends TestCase {

	MongoDBImpl	mongo;

	@Reference
	void setMongo(MongoDBImpl mongo) throws Exception {
		this.mongo = mongo;
	}

	public void setUp() throws Exception {
		DummyDS ds = new DummyDS();
		ds.add(this);
		ds.add(MongoDBImpl.class).$("db", "test");
		ds.add(new DummyLog().direct().stacktrace());
		ds.wire();
	}

	MongoCodec	mc	= new MongoCodec(null);

	public void testConverter() throws Exception {
		assertEquals("A", mc.toMongo('A'));
	}

	public static class Complex {
		public int		a	= 1;
		public String	b	= "x";
	}

	public static class TestData {
		public String			_id;
		public Version			version;
		public List<String>		packages;
		public byte				b;
		public char				c;
		public float			f;
		public double			d;
		public boolean			bo;
		public byte[]			data;
		public char[]			chars;
		public int[]			ints;
		public boolean[]		booleans;
		public float[]			floats;
		public double[]			doubles;
		public long[]			longs;
		public short[]			shorts;
		public List<Complex>	complex	= new ArrayList<Complex>();
	}

	public void testBasic() throws Exception {
		MongoStoreImpl<TestData> store = mongo.getStore(TestData.class, "testdata");
		store.all().remove();
		TestData a1 = new TestData();
		a1._id = "com.libsync.store";
		a1.version = new Version("1.0.0");
		a1.packages = new ArrayList<String>();
		a1.packages.add("p1");
		a1.packages.add("p2");
		a1.data = "hello world".getBytes();
		a1.chars = "XYZ".toCharArray();
		a1.c = 'A';
		a1.ints = new int[] {
				1, 2, 3
		};
		a1.booleans = new boolean[] {
				false, true
		};
		a1.shorts = new short[] {
				1, 2
		};
		a1.longs = new long[] {
				1, 2
		};
		a1.floats = new float[] {
				1, 2
		};
		a1.doubles = new double[] {
				1, 2
		};
		a1.complex.add(new Complex());
		store.upsert(a1);

		boolean further = false;
		for (TestData a : store.find("_id=*")) {
			assertFalse(further);
			assertNotNull(a._id);
			assertEquals("com.libsync.store", a._id);
			assertEquals(new Version("1.0.0"), a.version);
			assertEquals("hello world", new String(a.data));
			assertEquals("XYZ", new String(a.chars));
			assertEquals('A', a.c);
			assertNotNull(a.packages);
			assertTrue(a.packages.contains("p1"));
			assertTrue(a.packages.contains("p2"));
			further = true;
		}
		assertTrue(further);
	}

	public static class UniqueData {
		public String		_id;
		public Version		version;
		public int			counter;
		public int			x;
		public List<String>	xs	= new ArrayList<String>();
	}

	public void testUnique() throws Exception {
		MongoStoreImpl<UniqueData> store = mongo.getStore(UniqueData.class, "unique");
		store.all().remove();
		UniqueData u = new UniqueData();
		assertNull(u._id);
		store.insert(u);
		assertNotNull(u._id);
		String id = u._id;
		try {
			store.insert(u);
			fail();
		}
		catch (Exception e) {
			// ok, no dups allowed
		}

		assertEquals(1, store.all().count());

		UniqueData g = store.find("_id=%s", id).one();
		assertNotNull(g);
		assertNull(g.version);
		g.version = new Version(1, 0, 0);
		store.update(g);
		assertEquals(1, store.all().count());

		UniqueData g2 = store.find("_id=%s", id).one();
		assertNotNull(g2);
		assertEquals(new Version(1, 0, 0), g2.version);

		UniqueData u2 = new UniqueData();
		assertNull(u2._id);
		store.insert(u2);

		assertEquals(1, store.find(g2).count());
		assertEquals(2, store.all().count());

		assertEquals(2, store.find("counter=0").count());
		assertEquals(0, store.find("counter=1").count());

		store.find("counter=0").inc("counter", 1).update();
		store.find(g2).inc("counter", -1).update();
		assertEquals(1, store.find("counter=0").count());
		store.find(g2).inc("counter", -1).update();
		store.all().set("x", -1).update();
		assertEquals(2, store.find("x=-1").count());

		store.all().unset("x").update();
		assertEquals(0, store.find("x=*").count());

		store.all().append("xs", "1", "2", "3").update();
		assertEquals(2, store.find("xs=3").count());
	}

	public static class Basic {
		public String	_id;
		public int		value;
		public String	string;
	}

	public void testFilter() throws Exception {
		MongoStoreImpl<Basic> store = mongo.getStore(Basic.class, "filter");
		store.unique("value");
		store.all().remove();

		for (int i = 0; i < 1000; i++) {
			Basic b = new Basic();
			b.value = i;
			if (i > 500)
				b.string = i + "";
			store.insert(b);
		}

		assertEquals(1000, store.all().count());
		assertEquals(500, store.find("value>=500").count());
		assertEquals(100, store.find("(&(value>=500)(value<600))").count());
		assertEquals(999, store.find("(!(value=500))").count());
		assertEquals(2, store.find("(|(value=500)(value=600)").count());
		assertEquals(499, store.find("string=*").count());
		assertEquals(99, store.find("string=5*").count());
		assertEquals(175, store.find("string=*5*").count());

		int n = 0;
		for (Basic b : store.find("(|(value=100)(value=600))").select("string")) {
			if (n == 0) {
				assertNull(b.string);
				assertEquals(0, b.value);
			} else if (n == 1) {
				assertEquals("600", b.string);
				assertEquals(0, b.value);
			} else
				fail();

			n++;
		}
		assertEquals(2, n);
	}

	/**
	 * test the file storage
	 */
	public static class FileData {
		public byte[]	_id;
		public File		f;
	}

	public void testFiles() throws Exception {
		MongoStoreImpl<FileData> store = mongo.getStore(FileData.class, "files");
		store.drop();

		FileData d = new FileData();
		d.f = File.createTempFile("filetest", ".tmp");
		IO.store("Hello", d.f);
		store.insert(d);
		d.f.delete();

		d = store.all().first();
		String s = IO.collect(d.f);
		assertEquals("Hello", s);

		d = store.all().select("_id").first();
		assertNull(d.f);
	}
}
