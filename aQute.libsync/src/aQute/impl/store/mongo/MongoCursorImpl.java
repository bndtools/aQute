package aQute.impl.store.mongo;

import java.lang.reflect.*;
import java.util.*;

import aQute.lib.converter.*;
import aQute.service.store.*;

import com.mongodb.*;

public class MongoCursorImpl<T> implements Iterable<T>, Cursor<T> {
	enum Ops {
		INC, SET, UNSET, ADD, REMOVE, APPEND;
	}

	static DBObject			EMPTY		= new BasicDBObject();
	static Converter		converter	= new Converter();
	final MongoStoreImpl<T>	store;
	List<T>					objects;
	DBObject				where;
	DBObject				options;
	DBObject				update;
	int						skip;
	int						limit;

	public MongoCursorImpl(MongoStoreImpl<T> store) {
		this.store = store;
	}

	public MongoCursorImpl<T> where(String ldap, Object... args)
			throws Exception {
		combine("$and", store.filter(ldap, args));
		return this;
	}

	public MongoCursorImpl<T> or(T t) throws Exception {
		if (objects == null)
			objects = new ArrayList<T>();
		objects.add(t);

		combine("$or", store.filter(t));
		return this;
	}

	public MongoCursorImpl<T> select(String... keys) {
		if (options == null)
			options = new BasicDBObject();
		for (String key : keys)
			options.put(key, 1);
		return this;
	}

	public MongoCursorImpl<T> slice(String key, int count) {
		if (options == null)
			options = new BasicDBObject();
		options.put(key, new BasicDBObject("$slice", count));
		return this;
	}

	public MongoCursorImpl<T> limit(int limit) {
		this.limit = limit;
		return this;
	}

	public MongoCursorImpl<T> skip(int skip) {
		this.skip = skip;
		return this;
	}

	public MongoCursorImpl<T> ascending(String field) {
		return orderBy(field, 1);
	}

	public MongoCursorImpl<T> descending(String field) {
		return orderBy(field, -1);
	}

	public T first() {
		limit = 1;
		return iterator().next();
	}

	public Iterator<T> iterator() {
		final DBCursor cursor = getDBCursor();

		return new Iterator<T>() {

			public boolean hasNext() {
				return cursor.hasNext();
			}

			public T next() {
				try {
					DBObject object = cursor.next();
					return store.from(object);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			public void remove() {
				cursor.remove();
			}
		};
	}

	/**
	 * Answer the distinct values for a given field.
	 * 
	 * @param field
	 * @return
	 * @throws Exception
	 */

	public List< ? > distinct(String field) throws Exception {
		assert skip == 0;
		assert limit == 0;
		assert options == null;

		Class< ? > to = store.type.getField(field).getType();
		List< ? > list;

		// Do we have a sub selection? Then use the filter
		// otherwise use the call without where clause
		if (where == null)
			list = store.db.distinct(field);
		else
			list = store.db.distinct(field, where);

		List<Object> result = new ArrayList<Object>(list.size());
		for (Object o : list) {
			result.add(converter.convert(to, o));
		}
		return result;
	}

	private DBCursor getDBCursor() {
		System.out.println("Filter: " + where);
		final DBCursor cursor = store.db.find(where, options);
		if (limit != 0)
			cursor.limit(limit);
		if (skip != 0)
			cursor.skip(skip);
		return cursor;
	}

	public void remove() {
		store.db.remove(where);
	}

	private MongoCursorImpl<T> orderBy(String field, int i) {
		if (options == null)
			options = new BasicDBObject();
		options.put(field, i);
		return this;
	}

	public int count() {
		DBCursor cursor = getDBCursor();
		return cursor.count();
	}

	public T one() {
		limit = 1;
		Iterator<T> one = iterator();
		if (one.hasNext())
			return one.next();
		else
			return null;
	}

	void combine(String type, DBObject filter) {
		if (where == null) {
			where = filter;
			return;
		}
		where = new BasicDBObject(type, Arrays.asList(where, filter));
	}

	public MongoCursorImpl<T> set(String field, Object value) throws Exception {
		combineUpdate(field, "$set", value);
		return this;
	}

	public MongoCursorImpl<T> unset(String field) throws Exception {
		combineUpdate(field, "$unset", null);
		return this;
	}

	public MongoCursorImpl<T> append(String field, Object... value)
			throws Exception {
		combineUpdate(field, "$pushAll", Arrays.asList(value));
		return this;
	}

	public MongoCursorImpl<T> remove(String field, Object... value)
			throws Exception {
		combineUpdate(field, "$pullAll", Arrays.asList(value));
		return this;
	}

	public MongoCursorImpl<T> inc(String field, Object value) throws Exception {
		combineUpdate(field, "$inc", value);
		return this;
	}

	public boolean isEmpty() {
		return count() == 0;
	}

	private void combineUpdate(String field, String op, Object value)
			throws Exception {
		if (update == null)
			update = new BasicDBObject();

		DBObject o = (DBObject) update.get(op);
		if (o == null)
			update.put(op, o = new BasicDBObject());

		assert store.checkField(field, value);
		if (value instanceof Enum)
			value = value.toString();
		o.put(field, value);
	}

	public int update() {
		WriteResult result = store.db.update(where == null ? EMPTY : where,
				update, false, true);
		store.error(result);
		return result.getN();
	}

	public MongoCursorImpl<T> in(String field, Object... values)
			throws Exception {
		return in(field, Arrays.asList(values));
	}

	public MongoCursorImpl<T> in(String field, Collection< ? > values)
			throws Exception {
		if (where == null)
			where = new BasicDBObject();

		BasicDBObject in = new BasicDBObject();
		List<Object> list = new ArrayList<Object>();
		Field f = store.type.getField(field);
		for (Object value : values) {

			// TODO need to consider collection fields ...

			list.add(converter.convert(f.getGenericType(), value));
		}
		where.put(field, in);
		return this;
	}
}
