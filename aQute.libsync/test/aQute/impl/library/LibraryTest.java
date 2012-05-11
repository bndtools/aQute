package aQute.impl.library;

import junit.framework.*;
import aQute.bnd.annotation.component.*;
import aQute.impl.cafs.*;
import aQute.impl.hazelcast.*;
import aQute.impl.store.mongo.*;
import aQute.service.cafs.*;
import aQute.service.library.*;
import aQute.test.dummy.ds.*;
import aQute.test.dummy.log.*;

public class LibraryTest extends TestCase {

	DummyDS			ds	= new DummyDS();
	CAFS			cafs;
	
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
		ds.add(LibraryImpl.class);
		ds.wire();
	}
	
	public void testSimple() throws Exception {
		LibraryImpl lib = ds.get(LibraryImpl.class);
		Item item = lib.checkin(getClass().getResource("test.jar"));
		assertNotNull(item);
		
	}
}
