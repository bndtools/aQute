package service.jobs;

import java.util.*;

public interface JobQueue {
	<T> Job<T> queue(T job, String name, long delay);
	Collection<Job<?>> jobs(String type);
	Job<?> getJob(String id);
}
