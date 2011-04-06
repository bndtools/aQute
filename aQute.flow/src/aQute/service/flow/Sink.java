package aQute.service.flow;

public interface Sink<T> {
	void consume(T t);
}
