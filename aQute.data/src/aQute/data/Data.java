package aQute.data;

import java.lang.reflect.*;
import java.util.*;

/**
 * This class implements a Data class. A Data class must only have public fields
 * that are primitive, String, Numeric, Collection, Map, or Data.
 * 
 */
public class Data implements Map<String, Object> {
	public int size() {
		return getFields().length;
	}

	private Field[] getFields() {
		return getClass().getFields();
	}

	private Field getField(String name) {
		try {
			return getClass().getField(name);
		}
		catch (NoSuchFieldException e) {
			return null;
		}
	}

	public boolean isEmpty() {
		return getFields().length == 0;
	}

	public boolean containsKey(Object key) {
		return getField((String) key) != null;
	}

	public boolean containsValue(Object value) {
		for (Field f : getFields()) {
			Object o;
			try {
				o = f.get(this);
				if (value == o || (value != null && value.equals(o)))
					return true;
			}
			catch (Exception e) {
				// ignore, should not happen since we get the fields
				// from our own class
			}
		}
		return false;
	}

	public Object get(Object key) {
		return get0(getField((String) key));
	}

	private Object get0(Field f) {
		try {
			return f.get(this);
		}
		catch (Exception e) {
			// ignore, should not happen since we get the fields
			// from our own class
		}
		return null;
	}

	public Object put(String key, Object value) {
		return put0(getField(key), value);
	}

	private Object put0(Field f, Object value) {
		try {
			Object o = f.get(this);
			f.set(this, value);
			return o;
		}
		catch (Exception e) {
			// ignore, should not happen since we get the fields
			// from our own class
		}
		return null;
	}

	public Object remove(Object key) {
		throw new UnsupportedOperationException(
				"Remove is not supported for data objects");
	}

	public void putAll(Map< ? extends String, ? extends Object> m) {
		for (Entry< ? extends String, ? extends Object> e : m.entrySet()) {
			put(e.getKey(), e.getValue());
		}
	}

	public void clear() {
		throw new UnsupportedOperationException(
				"Clear is not supported for data objects");
	}

	public Set<String> keySet() {
		Set<String> s = new HashSet<String>();
		for (final Field f : getFields())
			s.add(f.getName());
		return s;
	}

	public Collection<Object> values() {
		Set<Object> s = new HashSet<Object>();
		for (Field f : getFields())
			s.add(get0(f));
		return s;
	}

	public Set<java.util.Map.Entry<String, Object>> entrySet() {
		Set<java.util.Map.Entry<String, Object>> s = new HashSet<Map.Entry<String, Object>>();
		for (final Field f : getFields()) {
			s.add(new Map.Entry<String, Object>() {

				public String getKey() {
					return f.getName();
				}

				public Object getValue() {
					return get0(f);
				}

				public Object setValue(Object value) {
					return put0(f, value);
				}
			});
		}
		return s;
	}

}
