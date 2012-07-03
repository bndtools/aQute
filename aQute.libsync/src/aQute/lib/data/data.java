package aQute.lib.data;

import java.lang.reflect.*;
import java.util.*;

import aQute.lib.converter.*;
import aQute.lib.json.*;

@SuppressWarnings("rawtypes")
public class data<T> implements Map<String,Object> {
	static Converter	converter	= new Converter();
	static JSONCodec	codec		= new JSONCodec().setIgnorenull(true);

	static class Cache {
		Map<String,Field>	fields	= new HashMap<String,Field>();
		Field[]				fs;
		String[]			names;
		Set<String>			keys	= Collections.unmodifiableSet(fields.keySet());
		int					size;
		Field				extra;
	}

	final static WeakHashMap<Class,Cache>	caches	= new WeakHashMap<Class,data.Cache>();
	private final Cache						cache;
	private final T							instance;

	public data(T instance) {
		this.instance = instance;
		this.cache = getCache(instance.getClass());
	}

	static synchronized Cache getCache(Class clazz) {
		Cache c = caches.get(clazz);
		if (c == null) {
			c = new Cache();
			List<Field> fs = new ArrayList<Field>();
			for (Field f : clazz.getFields()) {
				if (!Modifier.isStatic(f.getModifiers())) {
					fs.add(f);
				}
			}
			c.fs = fs.toArray(new Field[fs.size()]);
			c.size = c.fs.length;

			for (Field f : c.fs) {
				if (f.getName().equals("__extra"))
					c.extra = f;
				c.fields.put(f.getName(), f);
			}
			c.names = c.fields.keySet().toArray(new String[c.size]);
			Arrays.sort(c.names);
			caches.put(clazz, c);
		}
		return c;
	}

	public int size() {
		return cache.size;
	}

	public boolean isEmpty() {
		return cache.size == 0;
	}

	public boolean containsKey(Object key) {
		return Arrays.binarySearch(cache.names, (String) key) >= 0;
	}

	public boolean containsValue(Object value) {
		return values().contains(value);
	}

	public Object get(Object key) {
		Field f = cache.fields.get(key);
		if (f == null)
			return null;

		try {
			return f.get(instance);
		}
		catch (Exception e) {
			// Should not happen since we only have publics
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public Object put(String key, Object value) {
		Field f = cache.fields.get(key);
		if (f == null) {
			if (cache.extra != null) {
				try {
					Map<String,Object> x = (Map) cache.extra.get(this);
					if (x == null)
						cache.extra.set(this, x = new HashMap());
					x.put(key, value);
				}
				catch (Exception e) {
					// Fall through to unsupported exception
				}
			}
			throw new UnsupportedOperationException("A struct requires an existing key");
		}

		try {
			Object converted = converter.convert(f.getGenericType(), value);
			Object old = f.get(instance);
			f.set(instance, converted);
			return old;
		}
		catch (Exception e) {
			// Should not happen since we only have publics
			throw new RuntimeException(e);
		}
	}

	public Object remove(Object key) {
		throw new UnsupportedOperationException("Cannot removed fields from a struct " + getClass().getName());
	}

	public void putAll(Map< ? extends String, ? extends Object> m) {
		for (Map.Entry< ? extends String, ? extends Object> e : m.entrySet()) {
			put(e.getKey(), e.getValue());
		}
	}

	public void clear() {
		throw new UnsupportedOperationException("Read only");
	}

	public Set<String> keySet() {
		return cache.keys;
	}

	public Collection<Object> values() {
		ArrayList<Object> set = new ArrayList<Object>();
		for (Field f : cache.fs) {
			try {
				set.add(f.get(instance));
			}
			catch (Exception e) {
				// Should not happen since we only have publics
				throw new RuntimeException(e);
			}
		}
		return set;
	}

	public Set<Map.Entry<String,Object>> entrySet() {
		return new Set<Map.Entry<String,Object>>() {

			public int size() {
				return cache.size;
			}

			public boolean isEmpty() {
				return cache.size == 0;
			}

			public boolean contains(Object o) {
				// TODO
				throw new UnsupportedOperationException("Hmm, too complicated to implement and not very useful I think");
			}

			public Iterator<Map.Entry<String,Object>> iterator() {
				return new Iterator<Map.Entry<String,Object>>() {
					int	n	= -1;

					public boolean hasNext() {
						return n + 1 < cache.size;
					}

					public java.util.Map.Entry<String,Object> next() {
						n++;
						return new Map.Entry<String,Object>() {

							public String getKey() {
								return cache.fs[n].getName();
							}

							public Object getValue() {
								try {
									return cache.fs[n].get(data.this);
								}
								catch (Exception e) {
									throw new RuntimeException(e);
								}
							}

							public Object setValue(Object value) {
								throw new UnsupportedOperationException("Read only");
							}
						};
					}

					public void remove() {
						throw new UnsupportedOperationException("Read only");
					}

				};
			}

			public Object[] toArray() {
				// TODO
				throw new UnsupportedOperationException("Hmm, too complicated to implement and not very useful I think");
			}

			public <X> X[] toArray(X[] a) {
				// TODO
				throw new UnsupportedOperationException("Hmm, too complicated to implement and not very useful I think");
			}

			public boolean add(java.util.Map.Entry<String,Object> e) {
				throw new UnsupportedOperationException("Read only");
			}

			public boolean remove(Object o) {
				throw new UnsupportedOperationException("Read only");
			}

			public boolean containsAll(Collection< ? > c) {
				// TODO
				throw new UnsupportedOperationException("Hmm, too complicated to implement and not very useful I think");
			}

			public boolean addAll(Collection< ? extends java.util.Map.Entry<String,Object>> c) {
				throw new UnsupportedOperationException("Read only");
			}

			public boolean retainAll(Collection< ? > c) {
				throw new UnsupportedOperationException("Read only");
			}

			public boolean removeAll(Collection< ? > c) {
				throw new UnsupportedOperationException("Read only");
			}

			public void clear() {
				throw new UnsupportedOperationException("Read only");
			}

		};
	}

	public boolean equals(Object other) {
		if (other.getClass() != getClass())
			return false;

		data otherData = (data) other;
		try {
			for (Field f : cache.fs) {
				Object thisObject = f.get(instance);
				Object otherObject = f.get(otherData.instance);
				if (thisObject != otherObject) {
					if (thisObject == null)
						return false;

					if (!thisObject.equals(otherObject))
						return false;
				}
			}
			return true;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public int hashCode() {
		return 1; // TODO
	}

	public String toString() {
		try {
			return codec.enc().put(instance).toString();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public T instance() {
		return instance;
	}

	public static <T> data<T> wrap(T instance) {
		return new data<T>(instance);
	}

	public static void assign(Object from, Object to, boolean override, String... fields) throws Exception {
		Cache fromCache = getCache(from.getClass());
		Cache toCache = getCache(to.getClass());

		if (fields.length == 0)
			fields = toCache.names;

		for (String name : fields) {
			Field toField = toCache.fields.get(name);
			if (toField == null)
				throw new IllegalArgumentException(name + " is not a field in " + new data<Object>(to));

			Field fromField = fromCache.fields.get(name);
			if (fromField == null)
				continue;

			Object value = fromField.get(from);
			Object converted = converter.convert(toField.getGenericType(), value);
			toField.set(to, converted);
		}
	}

	public static void assign(Object from, Object to, String... fields) throws Exception {
		assign(from, to, true, fields);
	}

	public static void assignIfNotSet(Object from, Object to, String... fields) throws Exception {
		assign(from, to, false, fields);
	}

	public static boolean isData(Object o) {
		Cache c = getCache(o.getClass());
		return c.size > 0;
	}

	public Type getType(String f) {
		return cache.fields.get(f).getGenericType();
	}

	public static Field[] fields(Class c) {
		return getCache(c).fs;
	}

	public static Field getField(Class c, String f) {
		return getCache(c).fields.get(f);
	}
}
