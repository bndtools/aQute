package aQute.service.flow;

public interface Producer<T> extends Source<T>{
	void produce() throws Exception;
}
