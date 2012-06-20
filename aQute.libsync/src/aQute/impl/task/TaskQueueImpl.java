package aQute.impl.task;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.*;

import org.osgi.service.log.*;

import aQute.bnd.annotation.component.*;
import aQute.configurable.*;
import aQute.lib.collections.MultiMap;
import aQute.lib.hex.*;
import aQute.lib.json.*;
import aQute.service.store.*;
import aQute.service.task.*;
import aQute.service.task.TaskQueue;

import com.hazelcast.core.*;

/**
 * A Task Queue is intended to run tasks in a clustered environment. It can
 * queue tasks described by a TaskData, this queue is then read by anybody on
 * the same clusters, the task will be executed correctly only once on any of
 * the clustered system that runs this component. This Tasks Queue first stores
 * a task in the db and then sends the task to the shared queue. This shared
 * queue generates events when tasks are inserted. These events are used to fill
 * a queue with tasks to be processed. This queue is read by a single thread.
 */
@Component(designate = TaskQueueImpl.Config.class)
public class TaskQueueImpl extends Thread implements TaskQueue {
	final static int			SWEEP_TIME		= 10 * 1000;
	final static int			JOIN_TIMEOUT	= 10 * 1000;
	final static JSONCodec		codec			= new JSONCodec();
	private static final int	MAX_FAILURES	= 3;

	static class Task {
		TaskData	data;
		String		reference;
	}

	private BlockingQueue<Task>			queue		= new LinkedBlockingQueue<Task>();
	private MultiMap<String,TaskWorker>	taskWorkers	= new MultiMap<String,TaskWorker>();

	Store<TaskData>						store;
	LogService							log;
	IMap<byte[],String>					activeTasks;
	Executor							executor;
	HazelcastInstance					instance;
	AtomicNumber						sweep;
	int									errors;
	Semaphore							throttle;
	int									latency;
	boolean								nosweep;

	/**
	 * Implementation section
	 */
	interface Config {
		int parallel();

		int latency();
	}

	private Config							config;

	private EntryListener<byte[],String>	listener	= //
														new EntryListener<byte[],String>() {
															public void entryAdded(EntryEvent<byte[],String> event) {
																try {
																	String value = event.getValue();
																	System.out.println("Added " + value + " "
																			+ activeTasks.size());
																	TaskData data = codec.dec().from(value)
																			.get(TaskData.class);
																	synchronized (taskWorkers) {
																		if (!taskWorkers.containsKey(data.type)) {
																			System.out.println("No worker for "
																					+ data.type);
																			return;
																		}
																	}
																	Task task = new Task();
																	task.data = data;
																	task.reference = event.getValue();
																	queue.add(task);
																}
																catch (Exception e) {
																	log.log(LogService.LOG_ERROR,
																			"Failed to add new item "
																					+ event.getValue()
																					+ " to task queue", e);
																}
															}

															public void entryRemoved(EntryEvent<byte[],String> event) {
																// Ignore
															}

															public void entryEvicted(EntryEvent<byte[],String> arg0) {}

															public void entryUpdated(EntryEvent<byte[],String> arg0) {
																System.out.println("Updated");
															}
														};

	/**
	 * Cancel a task
	 */
	public boolean cancel(byte[] id) throws Exception {
		TaskData td = new TaskData();
		td._id = id;
		activeTasks.remove(id);
		return store.find(td).where("state=QUEUED").set("state", TaskData.State.CANCELED).update() == 1;
	}

	/**
	 * Get the Task Data
	 */
	public TaskData getTask(byte[] id) throws Exception {
		TaskData td = new TaskData();
		td._id = id;
		return store.find(td).one();
	}

	/**
	 * Lists the existing tasks
	 */
	public Iterator<TaskData> getTasks(String filter) throws Exception {
		if (filter == null)
			return store.all().iterator();
		else
			return store.find(filter).iterator();
	}

	/**
	 * 
	 */

	public <T> Builder<T> with(T work) throws Exception {
		return new BuilderImpl<T>(this, work);
	}

	@Activate
	void activate(Map<String,Object> props) {
		config = Configurable.createConfigurable(Config.class, props);
		throttle = new Semaphore(config.parallel() == 0 ? 2 : config.parallel());
		activeTasks = instance.getMap(getClass().getName() + ".map");
		sweep = instance.getAtomicNumber(getClass().getName() + ".time");
		activeTasks.addEntryListener(listener, true);
		if (config.latency() <= 0)
			latency = 60 * 1000;
		else
			latency = config.latency() * 1000;

		start();
	}

	@Deactivate
	void deactivate() throws InterruptedException {
		activeTasks.removeEntryListener(listener);
		interrupt();
		join(JOIN_TIMEOUT);
	}

	/**
	 * This is a new Task Data that is ready to run or must wait until it is
	 * ready to run. If it must wait we store it with the state set to
	 * State.TIMED_WAIT. Otherwise, we save it as delayed but state to QUEUED.
	 * However, we schedule it for execution. The idea is that before it is
	 * scheduled we move it to SUCCEEDED.
	 * 
	 * @param td
	 * @throws Exception
	 */
	void insert(TaskData td) throws Exception {
		long now = System.currentTimeMillis();

		if (td.after > now + 10) {
			store.insert(td);
		} else {
			td.after = now + latency;
			store.insert(td);
			String value = codec.enc().put(td).toString();
			activeTasks.put(td._id, value);
		}
	}

	/**
	 * Create a reference to the task
	 */

	/**
	 * Thread routine that will poll the queue and execute the task.
	 */

	public void run() {
		long deadline = sweep.get();
		assert deadline >= 0;

		while (true)
			try {
				long now = System.currentTimeMillis();
				long delay = deadline - now;
				if (!nosweep) {
					if (delay <= 0) {

						// We timed out, so see if we can get the
						// the lock on this sweep period

						long nextDeadline = now + getSweepTime();
						if (sweep.compareAndSet(deadline, nextDeadline)) {
							deadline = nextDeadline;
							sweep();
						} else
							// Another instance beat us, get its new deadline
							deadline = sweep.get();

						assert deadline > System.currentTimeMillis() + 100;
						continue;
					}
				} else
					delay = 10000;

				// Get some queued task data
				final Task task = queue.poll(delay, TimeUnit.MILLISECONDS);
				if (task == null)
					continue;

				// Throttle the number of active tasks
				throttle.acquire();

				System.out.println("Contains (should be true) " + activeTasks.containsKey(task.data._id));
				if (activeTasks.remove(task.data._id) == null) {
					// The task was already gone for some reason.
					// Which is perfectly legal
					throttle.release();
					continue;
				}
				System.out.println("Contains (should be false) " + activeTasks.containsKey(task.data._id));
				final TaskData td = task.data;
				Runnable r = new Runnable() {

					public void run() {
						try {
							TaskWorker taskWorker;

							synchronized (taskWorkers) {
								List<TaskWorker> typeWorkers = taskWorkers.get(td.type);

								// Check if we actually have a worker for the
								// type
								if (typeWorkers == null || typeWorkers.isEmpty()) {
									activeTasks.put(td._id, task.reference);
									return;
								}
								taskWorker = typeWorkers.get(0);
							}

							try {

								// Do the actual work!
								taskWorker.execute(td);

								errors = 0;
								int count;
								long now = System.currentTimeMillis();
								if (td.periodic <= 0) {
									System.out.println("Success ");
									count = store.find(td).set("state", TaskData.State.SUCCEEDED)
											.set("stateChange", now).update();
								} else {
									System.out.println("Reschedule " + now + td.periodic);
									count = store.find(td).set("after", now + td.periodic).set("stateChange", now)
											.update();

								}
								assert count == 1;

								return;
							}
							catch (InvocationTargetException ite) {
								log.log(LogService.LOG_ERROR, "Task worker failed task " + Hex.toHexString(td._id),
										ite.getCause());

							}
							catch (Throwable e) {
								log.log(LogService.LOG_ERROR, "Task worker failed task " + Hex.toHexString(td._id), e);

							}

						}

						catch (Exception e) {
							// the loop has a problem, not good
							log.log(LogService.LOG_ERROR, "Task Worker failed", e);
						}
						finally {
							throttle.release();
						}
					}

				};
				executor.execute(r);
			}
			catch (InterruptedException e) {
				System.out.println("Quiting");
				return;
			}
			catch (Exception e) {
				e.printStackTrace();
				// the loop has a problem, not good
				log.log(LogService.LOG_ERROR, "The main task loop failed, will sleep", e);
				try {
					// make sure we do not overload the system
					errors++;
					Thread.sleep(1000 * errors);
				}
				catch (InterruptedException e1) {
					interrupt();
				}
			}
	}

	private int getSweepTime() {
		int sweeptime = latency / 2;
		assert sweeptime >= 1000;
		return sweeptime;
	}

	/**
	 * The executor
	 * 
	 * @param executor
	 */
	@Reference
	void setExecutor(Executor executor) {
		this.executor = executor;
	}

	/**
	 * Log service
	 * 
	 * @param l
	 */
	@Reference
	void setLog(LogService log) {
		this.log = log;
	}

	/*
	 * Implementation
	 */

	@Reference(type = '*')
	synchronized void addWorker(Worker< ? > l) {
		TaskWorker ll = new TaskWorker(l);
		taskWorkers.add(ll.type.getName(), ll);
	}

	synchronized void removeWorker(Worker< ? > l) {
		TaskWorker ll = new TaskWorker(l);
		taskWorkers.remove(ll.type.getName(), ll);
	}

	@Reference
	void setHazelcast(HazelcastInstance hazelcast) {
		instance = hazelcast;
	}

	@Reference
	public void setDB(DB db) throws Exception {
		this.store = db.getStore(TaskData.class, "TaskData");
	}

	void clearForTest() throws Exception {
		activeTasks.clear();
		store.all().remove();
	}

	/**
	 * Sweep to verify that all to be active tasks are actually
	 * 
	 * @throws Exception
	 */
	public void sweep() throws Exception {
		long now = System.currentTimeMillis();
		for (TaskData td : store.find("&(after<%s)(state=QUEUED)", now)) {
			if (td.before <= now || td.failures > MAX_FAILURES) {
				store.find(td).set("state", TaskData.State.EXPIRED);
				activeTasks.remove(td._id);
			} else {
				store.find(td).set("after", now + latency).update();
				System.out.println("Before insertion " + activeTasks.size());
				activeTasks.put(td._id, codec.enc().put(td).toString());
				System.out.println("After insertion " + activeTasks.size());
			}
		}
	}
}
