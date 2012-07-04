package aQute.service.locks;

public interface LockManager {
	interface Lock {
		boolean unlock();

		String[] getLockedNames();
	}

	Lock lock(long maxWaitInMs, String... names);

}
