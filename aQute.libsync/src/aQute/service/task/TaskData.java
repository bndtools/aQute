package aQute.service.task;

import java.io.*;
import java.util.*;

public class TaskData implements Serializable {
	private static final long	serialVersionUID	= 1L;

	public enum State {
		QUEUED, SUCCEEDED, EXPIRED, CANCELED;
	}

	public byte[]		_id;
	public State		state;
	public long			stateChange;
	public long			before;
	public long			after;
	public long			periodic;
	public List<String>	log;
	public int			failures;
	public String		type;
	public String		task;

	public boolean equals(Object o) {
		if (!(o instanceof TaskData))
			return false;
		return Arrays.equals(_id, ((TaskData) o)._id);
	}

	public int hashCode() {
		return Arrays.hashCode(_id);
	}

	public String toString() {
		return type + "-" + task;
	}
}
