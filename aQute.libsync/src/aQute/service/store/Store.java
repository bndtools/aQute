package aQute.service.store;

public interface Store<T> {
	public void insert(T document) throws Exception;

	public void update(T document) throws Exception;

	public void upsert(T document) throws Exception;

	public Cursor<T> all() throws Exception;

	public Cursor<T> find(String where, Object... args) throws Exception;

	public Cursor<T> find(T select) throws Exception;

	public Cursor<T> select(String... keys);

	public byte[] uniqueId();

	public Cursor<T> optimistic(T p) throws Exception;

}
