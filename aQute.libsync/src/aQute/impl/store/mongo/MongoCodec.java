package aQute.impl.store.mongo;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

import org.osgi.service.log.*;

import aQute.lib.converter.*;
import aQute.lib.converter.Converter.Hook;
import aQute.lib.data.*;
import aQute.lib.hex.*;
import aQute.lib.io.*;
import aQute.lib.json.*;
import aQute.libg.cryptography.*;

import com.mongodb.*;
import com.mongodb.gridfs.*;

/*
 * TODO needs to handle files
 */
public class MongoCodec {
	Converter					converter	= new Converter().hook(File.class, new Hook() {

												public Object convert(Type dest, Object o) throws Exception {
													if (o instanceof String)
														return retrieveFile((String) o);
													return null;
												}

											});
	final static JSONCodec		codec		= new JSONCodec().setIgnorenull(true);
	final MongoStoreImpl< ? >	store;

	MongoCodec(MongoStoreImpl< ? > store) {
		this.store = store;
	}

	// /**
	// * TODO rewrite to be more efficient. Now it just encodes it JSON and
	// decodes it.
	// * @param o
	// * @return
	// * @throws Exception
	// */
	// Object encode(Object o) throws Exception {
	// StringWriter sw = new StringWriter();
	// codec.enc().to(sw).writeDefaults().put(o).flush();
	// return codec.dec().from(sw.toString()).get(BasicDBObject.class);
	// }
	//
	// /**
	// * TODO rewrite to be more efficient. Now it just encodes it JSON and
	// decodes it.
	// * @param o
	// * @return
	// * @throws Exception
	// */
	// <T> T decode(Class<T> type, Object o) throws IOException, Exception {
	// StringWriter sw = new StringWriter();
	// codec.enc().to(sw).put(o).flush();
	// return codec.dec().from(sw.toString()).get(type);
	// }
	//

	protected Object toFile(Object o) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Convert from an arbitrary object to an object that can be serialized by
	 * Mongo
	 * 
	 * @param name
	 * @return
	 * @throws Exception
	 */
	Object toMongo(Object o) throws Exception {
		if (o == null)
			return o;

		if (o instanceof File)
			return storeFile((File) o);

		if (o instanceof Number || o instanceof String || o instanceof DBObject)
			return o;

		if (o instanceof Iterable) {
			BasicDBList list = new BasicDBList();
			for (Object oo : (Iterable< ? >) o)
				list.add(toMongo(oo));

			return list;
		}

		if (o.getClass().isArray()) {
			Class< ? > component = o.getClass().getComponentType();
			if (component.isPrimitive())
				if (component == char.class)
					return new String((char[]) o);
				else
					return o;

			BasicDBList list = new BasicDBList();
			for (Object oo : (Object[]) o)
				list.add(toMongo(oo));

			return list;
		}

		if (o instanceof Map) {
			Map< ? , ? > map = (Map< ? , ? >) o;
			BasicDBObject dbo = new BasicDBObject();
			for (Map.Entry< ? , ? > entry : map.entrySet()) {
				dbo.put(entry.getKey().toString(), toMongo(entry.getValue()));
			}
		}

		if (data.isData(o)) {
			BasicDBObject dbo = new BasicDBObject();
			for (Field f : data.fields(o.getClass())) {
				Object value = toMongo(f.get(o));
				if (value != null)
					dbo.put(f.getName(), value);
			}
			return dbo;
		}

		if (o instanceof Character) {
			return "" + o;
		}

		return o.toString();
	}

	Object fromMongo(Type type, Object o) throws Exception {
		return converter.convert(type, o);
	}

	// TODO needs to handle the files.
	private Object retrieveFile(String name) throws Exception {
		if (name == null)
			return null;

		GridFSDBFile file = store.getGridFs().findOne(name);
		File out = File.createTempFile("mongostore", "sha");
		FileOutputStream fout = new FileOutputStream(out);
		try {
			Digester<SHA1> digester = SHA1.getDigester(fout);
			IO.copy(file.getInputStream(), digester);
			String calculated = Hex.toHexString(digester.digest().toByteArray());
			if (!calculated.equals(name)) {
				store.handler.log.log(LogService.LOG_ERROR,
						"Received invalid file from gridfs, sha does not match. Got " + calculated + " expected "
								+ name);
				return null;
			}
			return out;
		}
		finally {
			fout.close();
		}
	}

	/**
	 * File objects are stored in the GridFS store under their hex SHA1.
	 * 
	 * @param file
	 * @return
	 * @throws Exception
	 */
	private String storeFile(File file) throws Exception {
		if (file == null)
			return null;

		Digester<SHA1> digester = SHA1.getDigester();
		FileInputStream fin = new FileInputStream(file);
		IO.copy(fin, digester);
		String name = Hex.toHexString(digester.digest().toByteArray());
		if (store.getGridFs().findOne(name) != null)
			return name;

		GridFSInputFile gf = store.getGridFs().createFile(file);
		gf.setFilename(name);
		gf.save();
		return gf.getFilename();
	}

	/*
	 * BasicDBObject bdo = new BasicDBObject(); for (Field f : fields) { if
	 * (!Modifier.isStatic(f.getModifiers())) { Object o = f.get(a); if (o !=
	 * null) { bdo.put(f.getName(), toBSON(o)); } } } return bdo;
	 * @SuppressWarnings({"rawtypes", "unchecked"}) Object toBSON(Object o)
	 * throws Exception { if (o instanceof DBObject || o instanceof DBRef || o
	 * instanceof DBCollection) return o; if (o instanceof File) return
	 * storeFile((File) o); if (o == null || o instanceof Number || o instanceof
	 * Boolean || o instanceof Date || o instanceof String || o instanceof
	 * ObjectId) return o; if (o.getClass().isArray()) { Class< ? > cctype =
	 * o.getClass().getComponentType(); if (cctype.isPrimitive()) { if (cctype
	 * != char.class) return o; return new String((char[]) o); } BasicDBList l =
	 * new BasicDBList(); for (Object member : (Object[]) o)
	 * l.add(toBSON(member)); return l; } if (o instanceof Iterable) {
	 * BasicDBList l = new BasicDBList(); for (Object member : (Iterable) o)
	 * l.add(toBSON(member)); return l; } if (o instanceof Map) { BasicDBObject
	 * bdo = new BasicDBObject(); Map map = (Map) o; for (Map.Entry entry :
	 * (Set<Map.Entry>) map.entrySet()) { bdo.put(entry.getKey().toString(),
	 * toBSON(entry.getValue())); } return bdo; } if ( data.isData(o)) return
	 * to(o); return o.toString(); } T instance = type.newInstance(); for (Field
	 * f : fields) { Object object; if (f.getType() == File.class) object =
	 * retrieveFile((String) dbo.get(f.getName())); else object =
	 * converter.convert(f.getGenericType(), dbo.get(f.getName())); if (object
	 * != null) f.set(instance, object); } return instance; private Object
	 * fromBson(String key, String value) throws Exception { Object o; try {
	 * Field f = type.getField(key); if (f.getType() == File.class) return
	 * retrieveFile(key); o = converter.convert(f.getGenericType(), value); }
	 * catch (Exception e) { o = converter.convert(Object.class, value); } //
	 * Enums are not properly handled by mongo if (o instanceof Enum) o = value;
	 * if (o instanceof Collection) { Collection< ? > c = (Collection< ? >) o;
	 * return c.iterator().next(); } if (o.getClass().isArray()) { return
	 * Array.get(o, 0); } return o; }
	 */
}
