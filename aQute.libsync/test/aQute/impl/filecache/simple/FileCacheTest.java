package aQute.impl.filecache.simple;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import junit.framework.*;

import org.mockito.*;
import org.osgi.framework.*;

import aQute.bnd.annotation.component.*;
import aQute.lib.io.*;
import aQute.service.filecache.*;
import aQute.test.dummy.ds.*;
import aQute.test.dummy.log.*;

public class FileCacheTest extends TestCase {
	File				tmpdir;
	private FileCache	cache;

	public void setUp() throws Exception {
		tmpdir = File.createTempFile("testcache", "");
		tmpdir.delete();
		tmpdir.mkdirs();

		BundleContext bc = Mockito.mock(BundleContext.class);
		Mockito.when(bc.getDataFile(Mockito.anyString())).thenReturn(tmpdir);

		DummyDS ds = new DummyDS();
		ds.add(bc);
		ds.add(this);
		ds.add(SimpleFileCacheImpl.class).$("maxLockTime", 1000).$("reapPeriod", 1)
				.$("cacheDir", tmpdir.getAbsolutePath());
		ds.add(new DummyLog().direct().stacktrace());
		ds.add(new Timer());
		ds.add(Executors.newFixedThreadPool(4));
		ds.wire();
	}

	public void tearDown() {
		IO.delete(tmpdir);
	}

	public void testSimple() throws Exception {
		final AtomicBoolean called = new AtomicBoolean(false);

		File f1 = cache.get("halleluja", new Callable<InputStream>() {

			@Override
			public InputStream call() throws Exception {
				called.set(true);
				return new ByteArrayInputStream("Hello World".getBytes());
			}

		});

		assertTrue(called.get());
		assertNotNull(f1);
		assertEquals("Hello World", IO.collect(f1));

		called.set(false);
		File f2 = cache.get("halleluja", new Callable<InputStream>() {

			@Override
			public InputStream call() throws Exception {
				called.set(true);
				return new ByteArrayInputStream("Hello World".getBytes());
			}

		});
		assertEquals(f1, f2);
		assertFalse(called.get());
	}

	@Reference
	void setCache(FileCache cache) {
		this.cache = cache;
	}
}
