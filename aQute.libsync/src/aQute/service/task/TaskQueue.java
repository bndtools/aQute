package aQute.service.task;

import java.util.*;

public interface TaskQueue {
	public interface Builder<T> {
		TaskData queue() throws Exception;

		Builder<T> after(long ms);

		Builder<T> before(long ms);

		Builder<T> expires(long ms);

		Builder<T> delay(long ms);

		Builder<T> periodic(long ms);

		Builder<T> ms();

		Builder<T> secs();

		Builder<T> minutes();

		Builder<T> days();
	}

	<T> Builder<T> with(T work) throws Exception;

	boolean cancel(byte[] id) throws Exception;

	TaskData getTask(byte[] id) throws Exception;

	Iterator<TaskData> getTasks(String filter) throws Exception;
}
