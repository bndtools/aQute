package aQute.service.task;

public interface Worker<T> {
	void execute(T work) throws Exception;
}
