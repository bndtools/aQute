package aQute.impl.store.mongo;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.regex.*;

import org.bson.types.*;

import aQute.lib.data.*;
import aQute.service.store.*;

import com.mongodb.*;
import com.mongodb.gridfs.*;

public class MongoStoreImpl<T> implements Store<T> {
	final static Pattern	SIMPLE_EXPR	= Pattern.compile("([^=><~*]+)\\s*(=|<=|>=|>|<|~=)\\s*([^\\s]+)");
	final MongoDBImpl		handler;
	final Class<T>			type;
	final DBCollection		db;
	GridFS					gridfs;
	final Field				_id;
	final Map<String,Field>	unique		= new HashMap<String,Field>();
	final Field				fields[];
	final MongoCodec		mcnv		= new MongoCodec(this);

	public MongoStoreImpl(MongoDBImpl handler, Class<T> type, DBCollection db) throws Exception {
		this.handler = handler;
		this.db = db;
		this.type = type;
		fields = type.getFields();
		Field tmp = null;
		for (Field f : fields) {
			if (f.getName().equals("_id"))
				tmp = f;
		}
		if (tmp == null)
			throw new IllegalArgumentException("No _id field, required");

		_id = tmp;
	}

	public MongoStoreImpl<T> unique(String... fields) throws Exception {
		DBObject keys = new BasicDBObject();
		DBObject options = new BasicDBObject().append("unique", true);
		for (String name : fields) {
			type.getField(name);
			keys.put(name, 1);
			unique.put(name, type.getField(name));
		}
		db.ensureIndex(keys, options);
		return this;
	}

	public void insert(T document) throws Exception {
		Object key = _id.get(document);
		if (key == null) {
			if (_id.getType() == byte[].class)
				_id.set(document, ObjectId.get().toByteArray());
			else if (_id.getType() == String.class)
				_id.set(document, ObjectId.get().toString());
			else
				throw new IllegalArgumentException(
						"Has no _id set and id cann not be created because it is not a byte[] or a String");
		}
		DBObject o = (DBObject) mcnv.toMongo(document);
		WriteResult result = db.insert(o);
		error(result);
		// DBObject dbo = result.getLastError();
		// System.out.println(dbo + " " + o);
	}

	public void update(T document) throws Exception {
		DBObject o = (DBObject) mcnv.toMongo(document);
		DBObject filter = filter(document);
		error(db.update(filter, o));
	}

	public void upsert(T document) throws Exception {
		DBObject o = (DBObject) mcnv.toMongo(document);
		DBObject filter = filter(document);
		error(db.update(filter, o, true, false));
	}

	public MongoCursorImpl<T> all() throws Exception {
		return new MongoCursorImpl<T>(this).where("_id=*");
	}

	public MongoCursorImpl<T> find(String where, Object... args) throws Exception {
		return new MongoCursorImpl<T>(this).where(where, args);
	}

	public MongoCursorImpl<T> find(T select) throws Exception {
		return new MongoCursorImpl<T>(this, select);
	}

	void error(WriteResult result) {
		System.out.println(result + " " + result.getError());
		if (result.getLastError() != null && result.getError() != null)
			throw new RuntimeException(result.getError());

	}

	/**
	 * Create a filter out of an LDAP expression.
	 * 
	 * @param where
	 * @param ldap
	 * @return
	 * @throws Exception
	 */
	DBObject filter(String ldap, Object... args) throws Exception {
		String formatted = String.format(ldap, args);
		if (!formatted.startsWith("("))
			formatted = "(" + formatted + ")";

		Reader r = new StringReader(formatted);
		return expr(r, r.read());
	}

	private DBObject expr(Reader ldap, int c) throws Exception {
		while (Character.isWhitespace(c))
			c = ldap.read();

		assert c == '(';
		DBObject query = new BasicDBObject();

		do {
			c = ldap.read();
		} while (Character.isWhitespace(c));

		switch (c) {
			case '&' : {
				List<DBObject> exprs = exprs(ldap);
				query.put("$and", exprs);
				break;
			}

			case '|' : {
				List<DBObject> exprs = exprs(ldap);
				query.put("$or", exprs);
				break;
			}

			case '!' : {
				DBObject expr = expr(ldap, ldap.read());
				query.put("$nor", Arrays.asList(expr));
				break;
			}

			case -1 :
				throw new EOFException();

			default :
				while (Character.isWhitespace(c))
					c = ldap.read();

				StringBuilder sb = new StringBuilder();
				boolean regex = false;

				while (true) {
					if (c < 0)
						throw new EOFException();

					if (c == '\\') {
						c = ldap.read();
						if (c < 0)
							throw new EOFException();
					} else if (c == '*') {
						regex = true;
						sb.append(".");
					} else if (c == ')')
						break;

					sb.append((char) c);
					c = ldap.read();
				}
				Matcher m = SIMPLE_EXPR.matcher(sb);
				if (!m.matches())
					throw new IllegalArgumentException("Not a valid LDAP expression " + sb);

				String key = m.group(1);
				String op = m.group(2);
				String value = m.group(3);

				if (op.equals("=")) {
					if (".*".equals(value))
						query.put(key, new BasicDBObject("$exists", true));
					else {

						if (regex) {
							query.put(key, new BasicDBObject("$regex", "^" + value));
							// TODO ensure valid regex for value
						} else
							query.put(key, fromBson(key, value));
					}
				} else if (op.equals(">"))
					query.put(key, new BasicDBObject("$gt", fromBson(key, value)));
				else if (op.equals(">="))
					query.put(key, new BasicDBObject("$gte", fromBson(key, value)));
				else if (op.equals("<"))
					query.put(key, new BasicDBObject("$lt", fromBson(key, value)));
				else if (op.equals("<="))
					query.put(key, new BasicDBObject("$lte", fromBson(key, value)));
				else if (op.equals("~="))
					query.put(key, new BasicDBObject("$regex", fromBson(key, value)).append("$options", "i"));
				// TODO ensure valid regex for value
				else
					throw new IllegalArgumentException("Unknown operator " + op);

				// TODO optimize by recognizing patterns that map to better
				// operators
		}
		return query;
	}

	private Object fromBson(String key, String value) throws Exception {
		Object result = value;
		Field field = data.getField(type, key);
		if (field != null) {
			result = mcnv.converter.convert(field.getGenericType(), result);
		}

		result = mcnv.toMongo(result);

		// In a query, we do not specify the
		// collection/array levels
		if (result instanceof Iterable) {
			return ((Iterable< ? >) result).iterator().next();
		} else if (result.getClass().isArray())
			return Array.get(result, 0);
		else
			return result;
	}

	private List<DBObject> exprs(Reader ldap) throws Exception {
		int c;
		do {
			c = ldap.read();
		} while (Character.isWhitespace(c));

		List<DBObject> list = new ArrayList<DBObject>();
		while (c == '(') {
			list.add(expr(ldap, c));

			// read ( for another or ) for close
			c = ldap.read();
		}
		return list;
	}

	BasicDBObject filter(T t) throws IllegalAccessException {
		BasicDBObject or = new BasicDBObject();
		Object id = _id.get(t);
		if (id != null)
			or.append("_id", id);
		else {
			for (Field unique : this.unique.values()) {
				Object u = unique.get(t);
				if (u != null)
					or.append(unique.getName(), u);
			}
		}
		return or;
	}

	boolean checkField(String field, Object value) throws Exception {
		Field f = type.getField(field);
		return f != null;
	}

	public MongoCursorImpl<T> select(String... keys) {
		return new MongoCursorImpl<T>(this).select(keys);
	}

	public byte[] uniqueId() {
		return new ObjectId().toByteArray();
	}

	GridFS getGridFs() {
		if (gridfs == null) {
			this.gridfs = new GridFS(db.getDB(), db.getName());

		}
		return gridfs;
	}

	public void drop() {
		db.drop();
	}

	/**
	 * TODO implement optimistic locking
	 */
	public Cursor<T> optimistic(T p) throws Exception {
		return find(p);
	}
}
