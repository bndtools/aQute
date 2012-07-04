package aQute.service.store;

import java.util.*;

public interface Cursor<T> extends Iterable<T> {

	public Cursor<T> where(String ldap, Object... args) throws Exception;

	public Cursor<T> or(T t) throws Exception;

	public Cursor<T> select(String... keys) throws Exception;

	public Cursor<T> slice(String key, int count) throws Exception;

	public Cursor<T> limit(int limit) throws Exception;

	public Cursor<T> skip(int skip) throws Exception;

	public Cursor<T> ascending(String field) throws Exception;

	public Cursor<T> descending(String field) throws Exception;

	public T first() throws Exception;

	public Iterator<T> iterator();

	/**
	 * Answer the distinct values for a given field.
	 * 
	 * @param field
	 * @return
	 * @throws Exception
	 */

	public List< ? > distinct(String field) throws Exception;

	public void remove() throws Exception;

	public int count() throws Exception;

	public T one() throws Exception;

	public Cursor<T> set(String field, Object value) throws Exception;

	public Cursor<T> set(String field) throws Exception;

	public Cursor<T> unset(String field) throws Exception;

	public Cursor<T> append(String field, Object... value) throws Exception;

	public Cursor<T> remove(String field, Object... value) throws Exception;

	public Cursor<T> inc(String field, Object value) throws Exception;

	public boolean isEmpty() throws Exception;

	public int update() throws Exception;

	public Cursor<T> in(String field, Object... values) throws Exception;

	public Cursor<T> in(String field, Collection< ? > values) throws Exception;
}
