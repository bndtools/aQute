package aQute.service.store;

public interface DB {
	<T> Store<T> getStore(Class<T> clazz, String name) throws Exception;
}
