package aQute.impl.task;

import java.lang.reflect.*;

import aQute.service.task.*;

/*
 * Represents a listener. It finds the used data type of the execute method
 * so that we can easily use it.
 */
class TaskWorker {
	Worker< ? >	worker;
	Class< ? >	type;
	Method		execute;
	long		blacklisted	= 0;
	int			errors;

	@SuppressWarnings("unchecked")
	public TaskWorker(Worker< ? > l) {
		this.worker = l;
		Method ms[] = l.getClass().getMethods();
		for (Method m : ms) {
			if (m.getName().equals("execute")) {
				if (m.getParameterTypes().length == 1) {
					type = (Class<Object>) m.getParameterTypes()[0];
					execute = m;
					break;
				}
			}
		}
		assert type != null;
	}

	void execute(TaskData data) throws Exception {
		Object task = TaskQueueImpl.codec.dec().from(data.task).get(type);
		execute.invoke(worker, task);
	}

	/*
	 * We take listener identity for equals semantics
	 */
	public boolean equals(Object o) {
		if (o instanceof TaskWorker)
			return worker == ((TaskWorker) o).worker;

		return false;
	}

	/**
	 * We let the listener provide the hashcode
	 */
	public int hashCode() {
		return worker.hashCode();
	}
}
