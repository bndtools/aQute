package aQute.service.flow;


public interface Source<T> {
	void setSink( Sink<T> end);
}
