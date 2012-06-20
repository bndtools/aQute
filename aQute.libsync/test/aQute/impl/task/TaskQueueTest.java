package aQute.impl.task;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import junit.framework.*;
import aQute.bnd.annotation.component.*;
import aQute.impl.hazelcast.*;
import aQute.impl.store.mongo.*;
import aQute.service.task.*;
import aQute.test.dummy.ds.*;
import aQute.test.dummy.log.*;

public class TaskQueueTest extends TestCase {
	TaskQueueImpl	tq;
	DummyDS			ds	= new DummyDS();

	@Reference
	void setTaskQueue(TaskQueueImpl tqi) throws Exception {
		this.tq = tqi;
		tqi.clearForTest();
	}

	public void setUp() throws Exception {
		ds.add(this);
		ds.add(MongoDBImpl.class).$("db", "test");
		ds.add(HazelcastImpl.class).$("multicast", false).$("name", "test");
		ds.add(new Timer());
		ds.add(Executors.newFixedThreadPool(4));
		ds.add(new DummyLog().direct().stacktrace().filter("Failed"));
		ds.add(TaskQueueImpl.class).$("latency", 30);
		ds.wire();
	}

	public static class Print {
		public String	message;
		public boolean	error;
	}

	public void testSimple() throws Exception {
		final Semaphore s = new Semaphore(0);
		final AtomicBoolean throwException = new AtomicBoolean(false);

		tq.addWorker(new Worker<Print>() {

			public void execute(Print work) {
				System.out.println(work.message + " " + throwException);
				if (throwException.get())
					throw new RuntimeException("Failed");

				s.release();
			}

		});
		Print p = new Print();
		p.message = "Hello world";

		tq.latency = 2000;
		tq.nosweep = true;

		{
			// Test a failure
			throwException.set(true);

			// TaskData td =
			TaskData td = tq.with(p).queue();
			Thread.sleep(1000);
			assertEquals(1, ds.get(DummyLog.class).getEntries().size());
			assertEquals(1, tq.store.find(td).where("&(state=QUEUED)(after>%s)", System.currentTimeMillis()).count());
			throwException.set(false);
			Thread.sleep(5000);
			tq.sweep();
			Thread.sleep(1000);
			s.acquire();
			assertEquals(1, tq.store.find(td).where("state=SUCCEEDED").count());
			assertTrue(ds.get(DummyLog.class).check("Failed"));
		}

		/*
		 * Test periodic and cancel
		 */
		{
			throwException.set(false);
			TaskData td = tq.with(p).secs().periodic(5).queue();
			Thread.sleep(1000); // wait for the db to be updated
			s.acquire();
			assertEquals(1, tq.store.find(td).where("state=QUEUED").count());
			Thread.sleep(5000);
			tq.sweep();
			Thread.sleep(1000); // wait for the db to be updated
			s.acquire();
			assertEquals(1, tq.store.find(td).where("state=QUEUED").count());

			tq.cancel(td._id);
			assertEquals(1, tq.store.find(td).where("state=CANCELED").count());
			Thread.sleep(5000);
			tq.sweep();
			Thread.sleep(1000); // wait for the db to be updated
			assertEquals(0, s.availablePermits());

			assertTrue(ds.get(DummyLog.class).check()); // no erorrs?
		}

		throwException.set(false);
		{
			// Test if the task data is executed
			TaskData td = tq.with(p).queue();

			s.acquire();

			Thread.sleep(1000); // wait for the db to be updated
			assertEquals(1, tq.store.find(td).where("state=SUCCEEDED").count());
			assertTrue(ds.get(DummyLog.class).check());

		}

		{
			// Test a timed wait, the sweep does not occur until much later
			// so we do not have to worry about that
			long now = System.currentTimeMillis();
			TaskData td = tq.with(p).after(now + 2000).queue();
			assertFalse(tq.activeTasks.containsKey(td._id));
			assertEquals(1, tq.store.find(td).where("state=QUEUED").count());

			Thread.sleep(3000); // timeout
			tq.sweep(); // ensure the sweeper runs
			s.acquire(); // has the worker been called?
			Thread.sleep(1000); // wait for the db to be updated
			assertEquals(1, tq.store.find(td).where("state=SUCCEEDED").count());
			assertTrue(ds.get(DummyLog.class).check()); // no erorrs?
		}

		Thread.sleep(1000);
		tq.deactivate();

	}

	/**
	 * Test if the sweeper does its job
	 */

	public void testSweeper() {

	}
}
