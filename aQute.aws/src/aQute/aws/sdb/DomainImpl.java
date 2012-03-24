package aQute.aws.sdb;

import java.lang.reflect.*;
import java.util.*;

import aQute.aws.*;
import aQute.aws.sdb.Domain.*;

/**
 * Implements a domain so that the interface to SDB is type safe, even though
 * SDB uses only strings.
 * 
 * A domain is associated with a record type (a class). Public fields in this
 * class are attributes in s3. There are the following type of attributes:
 * <ul>
 * <li><b>id</b> - The id attribute is the name of the item.
 * <li><b>map type</b> - There can be one Map the item. Entries in the map must
 * not shadow field names.
 * <li><b>strings</b> - Strings are set/get as is without handling
 * <li><b>integer numbers</b> - Will be stored with leading zeroes
 * <li><b>collections</b> - Collections are treated as multi value attributes.
 * Please not there are severe constraints on the number of attributes!
 * <li><b>other</b> - Scalar types must be convertible to/from string with the
 * JSON Encoder. Most scalar object types are supported as long as they include
 * a toString() that can be read by the constructor.
 * </ul>
 * 
 * @param <T> The domain type
 */
public class DomainImpl<T> {
	final Map<String, FieldHandler>	handlers	= new LinkedHashMap<String, FieldHandler>();
//	final JSONEncoder				codec		= new JSONEncoder();
	final MapFieldHandler			mapHandler;
	final FieldHandler				idHandler;
	final SDBImpl					parent;
	final String					name;
	final Class<T>					type;

	/**
	 * The constructor initializes the domain and sets up handlers for the
	 * fields. It will not verify if the domain actually exists.
	 * 
	 * TODO should we lazily create it?
	 * 
	 * @param parent The parent SDB class
	 * @param type the type for this domain
	 * @param name the name of this domain
	 */

	@SuppressWarnings("unchecked")
	DomainImpl(SDBImpl parent, Class<T> type, String name) throws Exception {
		this.type = type;
		this.name = name;

		FieldHandler map = null;
		FieldHandler id = null;

		for (Field field : type.getFields()) {
			String fieldName = field.getName();
			Class< ? > clazz = field.getType();

			FieldHandler handler;

			if (clazz.isArray())
				handler = new ArrayFieldHandler(field);
			else

				if (Collection.class.isAssignableFrom(clazz))
					handler = new CollectionFieldHandler(field);
				else
					if (Map.class.isAssignableFrom(clazz))
						handler = new MapFieldHandler(field);
					else
						// TODO check if scalar has proper type
						handler = new FieldHandler(field);

			if (fieldName.equals("_map"))
				map = handler;
			else
				if (fieldName.equals("id"))
					id = handler;
				else
					handlers.put(fieldName, handler);
		}

		if (id == null)
			throw new IllegalArgumentException("Specify an id field");

		if (map != null && !(MapFieldHandler.class.isInstance(map)))
			throw new IllegalArgumentException(
					"A _map is specified but it is not a map");

		this.mapHandler = (MapFieldHandler) map;
		this.idHandler = id;
		this.parent = parent;
	}

	/**
	 * Update the items by creating a request.
	 * 
	 * Each item's attributes are used create a request. This will use the
	 * BatchPutAttributes request.
	 * 
	 * @param items The items to update.
	 */
	public MultiRequest<T> item(T item) {
//		final List<T> items = new ArrayList<T>();
//		items.add(item);
//
//		return new MultiRequest<T>() {
//
//			@Override
//			public MultiRequest<T> item(T item) {
//				items.add(item);
//				return this;
//			}
//
//			@Override
//			public void put() throws Exception {
//				final Request request = new Request(parent, name,
//						"BatchPutAttributes");
//
//				for (T item : items) {
//					Object uid = idHandler.field.get(item);
//					if (uid == null)
//						throw new IllegalArgumentException(
//								"Item must not have a null id: "
//										+ idHandler.field);
//
//					request.addItem(asString(String.class, uid));
//
//					for (FieldHandler handler : handlers.values())
//						handler.put(request, item);
//
//					if (mapHandler != null)
//						mapHandler.updateSpecial(request, item);
//				}
//
//				request.execute();
//			}
//
//			@Override
//			public void delete() throws Exception {
//				final Request request = new Request(parent, name,
//						"BatchDeleteAttributes");
//
//				for (T item : items) {
//					Object uid = idHandler.field.get(item);
//					if (uid == null)
//						throw new IllegalArgumentException(
//								"Item must not have a null id: "
//										+ idHandler.field);
//
//					request.addItem(asString(String.class, uid));
//				}
//				request.execute();
//
//			}
//
//		};
		return null;
	}

	/**
	 * Get an object from the server. This will result in a GetAttributes
	 * request.
	 */

	public T get(Object id) throws Exception {
//		Request request = new Request(parent, name, "GetAttributes");
//		request.set("ItemName", asString(idHandler.field.getGenericType(), id));
//
//		T item = type.newInstance();
//		idHandler.field.set(item, id); // Type check ...
//
//		InputStream in = request.execute();
//
//		DocumentBuilder db = SDB.dbf.newDocumentBuilder();
//		Document doc = db.parse(in);
//		XPath xpath = SDB.xpf.newXPath();
//
//		NodeList nodes = (NodeList) xpath.evaluate(
//				"/GetAttributesResponse/GetAttributesResult/Attribute", doc,
//				XPathConstants.NODESET);
//
//		for (int i = 0; i < nodes.getLength(); i++) {
//			String key = xpath.evaluate("Name", nodes.item(i));
//			String value = xpath.evaluate("Value", nodes.item(i));
//			FieldHandler handler = handlers.get(key);
//			if (handler != null)
//				handler.setValue(item, value);
//			else
//				if (mapHandler != null) {
//					mapHandler.setSpecialValue(item, key, value);
//				}
//		}
//		return item;
		return null;
	}


	String asString(Type type, Object object) throws Exception {
//		if (object instanceof String)
//			return (String) object;
//
//		StringBuilder sb = new StringBuilder();
//		codec.encode(sb, type, object);
//		return sb.toString();
		return null;
	}

	Object toObject(Type type, String s) throws Exception {
//
//		if (type == String.class)
//			return s;
//
//		return codec.decode(type, new StringReader(s));
		return null;
	}

	class FieldHandler {
		final Field		field;
		final String	name;

		FieldHandler(Field field) {
			this.field = field;
			this.name = field.getName();
		}

		public void setValue(T tuple, String value) throws Exception {
			field.set(tuple, toObject(field.getGenericType(), value));
		}

		/**
		 * Scalar update
		 * 
		 * @param deltas
		 * @param uid
		 * @param updated
		 * @throws IllegalArgumentException
		 * @throws IllegalAccessException
		 */
		public void put(Request args, T updated) throws Exception {
//			Object value = field.get(updated);
//			args.addAttribute(name, asString(field.getGenericType(), value),
//					true);
		}
	}

	@SuppressWarnings("unchecked")
	class CollectionFieldHandler extends FieldHandler {
		final Type	memberType;

		CollectionFieldHandler(Field field) {
			super(field);
			memberType = ((ParameterizedType) field.getGenericType())
					.getActualTypeArguments()[0];
		}

		@Override
		public void setValue(T tuple, String value) throws Exception {
//			Collection<Object> collection = (Collection<Object>) field
//					.get(tuple);
//			if (collection == null) {
//				collection = newCollection(field.getType());
//				field.set(tuple, collection);
//			}
//
//			collection.add(toObject(field.getGenericType(), value));
		}

		@Override
		public void put(Request args, T updated) throws Exception {
//			Collection< ? > values = (Collection< ? >) field.get(updated);
//			for (Object value : values)
//				args.addAttribute(name, asString(memberType, value), true);
		}

	}

	@SuppressWarnings("unchecked")
	class MapFieldHandler extends FieldHandler {
		final Type	keyType;
		final Type	valueType;
		final Type	collectionType;

		MapFieldHandler(Field field) {
			super(field);
			keyType = ((ParameterizedType) field.getGenericType())
					.getActualTypeArguments()[0];
			valueType = ((ParameterizedType) field.getGenericType())
					.getActualTypeArguments()[1];

			if (valueType instanceof ParameterizedType) {
				ParameterizedType pt = (ParameterizedType) valueType;
				if (Collection.class.isAssignableFrom((Class< ? >) pt
						.getRawType()))
					collectionType = pt.getActualTypeArguments()[0];
				else
					collectionType = null;
			}
			else
				collectionType = null;
		}

		@Override
		public void setValue(T item, String s) throws Exception {
//			Map<Object, Object> map = (Map<Object, Object>) field.get(item);
//
//			if (map == null)
//				map = newMap(field.getType());
//
//			String split[] = s.split("\n");
//			Object key = toObject(keyType, split[0]);
//			Object value = toObject(valueType, split[1]);
//
//			map.put(key, value);
		}

		/**
		 * 
		 */
		@Override
		public void put(Request args, T updated) throws Exception {
//			Map< ? , ? > values = (Map< ? , ? >) field.get(updated);
//			if (values == null) {
//				values = newMap(field.getGenericType());
//				field.set(updated, values);
//			}
//			for (Map.Entry< ? , ? > entry : values.entrySet()) {
//				String key = asString(keyType, entry.getKey());
//
//				// TODO Check use of existing keys
//
//				if (collectionType != null) {
//					// Handle multiple attrs
//					Collection< ? > members = (Collection< ? >) entry
//							.getValue();
//					for (Object o : members) {
//						args.addAttribute(key, asString(collectionType, o),
//								true);
//					}
//				}
//				else {
//					// Handle scalar
//					args.addAttribute(key,
//							asString(collectionType, entry.getValue()), true);
//				}
//			}
		}

		public void setSpecialValue(T tuple, String name, String v)
				throws Exception {
			Object key = toObject(keyType, name);

			Map<Object, Object> map = (Map<Object, Object>) field.get(tuple);
			if (map == null) {
//				map = newMap(field.getType());
			}
			if (collectionType == null) {
				Object value = toObject(valueType, v);
				map.put(key, value);
			}
			else {
				Object value = toObject(collectionType, v);
				Collection<Object> collection = (Collection<Object>) map
						.get(key);
				if (collection == null) {
//					map.put(key, newCollection(valueType));
				}
				collection.add(value);
			}
		}

		public void updateSpecial(Request args, T updated) throws Exception {
			Map< ? , ? > values = (Map< ? , ? >) field.get(updated);

			for (Map.Entry< ? , ? > entry : values.entrySet()) {
				String key = asString(keyType, entry.getKey());

				// TODO Check use of existing keys

				if (collectionType != null) {
					// Handle multiple attrs
					Collection< ? > members = (Collection< ? >) entry
							.getValue();
					for (Object o : members) {
//						args.addAttribute(key, asString(collectionType, o),
//								true);
					}
				}
				else {
					// Handle scalar
//					args.addAttribute(key,
//							asString(collectionType, entry.getValue()), true);
				}
			}
		}
	}

	class ArrayFieldHandler extends FieldHandler {
		final Type	memberType;

		ArrayFieldHandler(Field field) {
			super(field);
			memberType = ((GenericArrayType) field.getGenericType())
					.getGenericComponentType();
		}

		@Override
		public void setValue(T tuple, String value) throws Exception {
			Object array = field.get(tuple);
			int size = 1;
			if (array == null) {
				array = Array
						.newInstance(field.getType().getComponentType(), 1);
			}
			else {
				size = Array.getLength(array) + 1;
				array = Array.newInstance(array.getClass().getComponentType(),
						size);
			}
			Array.set(array, size - 1, toObject(memberType, value));
		}

		@Override
		public void put(Request args, T updated) throws Exception {
			Object array = field.get(updated);
//			for (int attribute = 0; attribute < Array.getLength(array); attribute++)
//				args.addAttribute(name,
//						asString(memberType, Array.get(array, attribute)), true);
		}
	}


	public MultiGetRequest<T> id(Object key) {
		// TODO Auto-generated method stub
		return new MultiGetRequest<T>(){

			@Override
			public MultiGetRequest<T> key(Object id) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Collection<T> get() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public void delete() {
				// TODO Auto-generated method stub
				
			}
			
		};
	}

	public aQute.aws.sdb.Domain.ConditionalRequest<T> conditional(T item)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
