package aQute.impl.task;

import java.util.concurrent.*;

import aQute.service.task.*;
import aQute.service.task.TaskQueue.Builder;

/**
 * Builds a TaskData and prepares or queues it.
 * 
 * @param <T>
 */
public class BuilderImpl<T> implements Builder<T> {
	final TaskQueueImpl	tqi;
	final TaskData		td		= new TaskData();
	TimeUnit			unit	= TimeUnit.MILLISECONDS;

	public BuilderImpl(TaskQueueImpl tqi, T work) throws Exception {
		this.tqi = tqi;
		td.task = TaskQueueImpl.codec.enc().put(work).toString();
		td.type = work.getClass().getName();
		td.before = Long.MAX_VALUE;
		td.after = 0;
		td.state = TaskData.State.QUEUED;
	}

	public TaskData queue() throws Exception {
		verify();
		tqi.insert(td);
		return td;
	}

	public Builder<T> after(long time) {
		this.td.after = unit.toMillis(time);
		return this;
	}

	public Builder<T> delay(long delay) {
		this.td.after = System.currentTimeMillis() + unit.toMillis(delay);
		return this;
	}

	public Builder<T> before(long time) {
		this.td.before = unit.toMillis(time);
		return this;
	}

	public Builder<T> expires(long delay) {
		this.td.before = System.currentTimeMillis() + unit.toMillis(delay);
		return this;
	}

	private void verify() {
		if (td.before < System.currentTimeMillis())
			throw new IllegalArgumentException("Expired before started");

		if (td.after > td.before)
			throw new IllegalArgumentException("Expires before it can run");

		if (td.periodic > 0 && td.periodic < 5000) {
			throw new IllegalArgumentException("Periodic must be at least 5 secs");
		}
	}

	public Builder<T> periodic(long time) {
		td.periodic = unit.toMillis(time);
		return this;
	}

	public Builder<T> ms() {
		unit = TimeUnit.MILLISECONDS;
		return this;
	}

	public Builder<T> secs() {
		unit = TimeUnit.SECONDS;
		return this;
	}

	public Builder<T> minutes() {
		unit = TimeUnit.MINUTES;
		return this;
	}

	public Builder<T> days() {
		unit = TimeUnit.DAYS;
		return this;
	}

}
