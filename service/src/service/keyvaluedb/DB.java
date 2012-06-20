package service.keyvaluedb;

public interface DB {

	<T> Domain<T> getDomain(Class<T> c);

	Blob createBlob();
}
