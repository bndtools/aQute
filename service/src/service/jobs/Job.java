package service.jobs;

public interface Job<T> {
	String getId();
	String getType();
	long getDeadline();
	void remove();
}
