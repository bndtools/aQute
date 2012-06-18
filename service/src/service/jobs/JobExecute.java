package service.jobs;

public interface JobExecute<T> {
	void execute(T job) throws Exception;
}
