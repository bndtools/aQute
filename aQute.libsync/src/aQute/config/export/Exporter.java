package aQute.config.export;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

import org.osgi.service.cm.*;

import aQute.lib.base64.*;
import aQute.lib.json.*;

@SuppressWarnings({
		"rawtypes", "unchecked"
})
public class Exporter {

	final static JSONCodec	codec	= new JSONCodec();

	public static class Export {
		public Map<String,Object>	__extra;
		public String				pid;
		public String				factoryPid;
		public Map<String,Object>	values;
		public Map<String,Type>		types;
	}

	public static class Type {
		public String	scalar;
		public String	vectorOf;
		public String	arrayOf;
	}

	public static class Protocol {
		public int					version;
		public Date					date;
		public Map<String,Object>	__extra;
		public int					size;
	}

	public static void toJSON(ConfigurationAdmin admin, Writer osw, String filter) throws Exception {

		Configuration[] list = admin.listConfigurations(filter);
		Encoder encoder = codec.enc().to(osw);

		Protocol p = new Protocol();
		p.version = 1;
		p.date = new Date();
		encoder.put(p).append('\n');

		if (list != null) {
			p.size = list.length;
			for (Configuration c : list) {
				Dictionary<String,Object> d = c.getProperties();
				Export export = new Export();
				export.values = new HashMap<String,Object>();
				export.factoryPid = c.getFactoryPid();
				export.pid = c.getPid();

				for (Enumeration<String> e = d.keys(); e.hasMoreElements();) {
					String k = e.nextElement();
					Object v = d.get(k);

					if (!(v instanceof String)) {

						if (export.types == null)
							export.types = new HashMap<String,Type>();

						Type type = new Type();

						Class< ? > clazz = v.getClass();
						if (v instanceof Collection) {
							Collection< ? > coll = (Collection< ? >) v;
							clazz = String.class;
							if (coll.size() > 0)
								type.vectorOf = shortName(coll.iterator().next().getClass());
							else
								type.vectorOf = shortName(String.class);
						} else if (v.getClass().isArray()) {
							type.arrayOf = shortName(clazz.getComponentType());
						} else
							type.scalar = shortName(v.getClass());

						export.types.put(k, type);
					}
					export.values.put(k, v);
				}

				encoder.mark().put(export);
				// encoder.put(encoder.digest());
				encoder.append('\n');
			}
		}
		osw.flush();
	}

	private static String shortName(Class clazz) {
		String name = clazz.getName();
		if (name.startsWith("java.lang."))
			return name.substring(10);
		else
			return name;
	}

	public static void fromJSON(Reader r, ConfigurationAdmin admin) throws Exception {

		Decoder decoder = codec.dec().from(r);
		Protocol p = decoder.get(Protocol.class);
		for (int i = 0; i < p.size; i++) {
			Export e = decoder.get(Export.class);
			Configuration c;
			if (e.factoryPid != null) {
				c = admin.createFactoryConfiguration(e.factoryPid, "?*");
			} else {
				c = admin.getConfiguration(e.pid, "?*");
			}

			Dictionary<String,Object> d = getDictionary(e.types, e.values);
			c.update(d);
		}

	}

	private static Dictionary<String,Object> getDictionary(Map<String,Type> types, Map<String,Object> values)
			throws Exception {
		Hashtable<String,Object> ht = new Hashtable<String,Object>();
		for (Map.Entry<String,Object> e : values.entrySet()) {
			String key = e.getKey();
			Object value = e.getValue();

			Type type;
			if (types != null && (type = types.get(key)) != null) {

				if (type.scalar != null) {
					value = convert(value, type.scalar);
				} else if (type.vectorOf != null) {
					Collection<Object> coll = (Collection<Object>) value;
					Vector vector = new Vector();
					for (Object o : coll) {
						vector.add(convert(o, type.vectorOf));
					}
					value = vector;
				} else if (type.arrayOf != null) {
					if (value instanceof String && type.arrayOf.equals("byte")) {
						value = Base64.decodeBase64((String) value);
					} else {
						Collection<Object> coll = (Collection<Object>) value;
						Object array = Array.newInstance(getClass(type.arrayOf), coll.size());
						int n = 0;
						for (Object o : coll) {
							Array.set(array, n++, convert(o, type.arrayOf));
						}
						value = array;
					}
				} else {
					throw new IllegalArgumentException("Key " + key
							+ " has type but neither scalar, vectorOf, nor arrayOf is set");

				}
			}
			ht.put(key, value);
		}
		return ht;
	}

	private static Object convert(Object o, String type) {
		if ("boolean".equals(type))
			return o;
		if ("Boolean".equals(type))
			return o;

		if ("byte".equals(type))
			return ((Number) o).byteValue();

		if ("short".equals(type))
			return ((Number) o).shortValue();

		if ("char".equals(type))
			return (char) ((Number) o).intValue();

		if ("int".equals(type))
			return ((Number) o).intValue();

		if ("long".equals(type))
			return ((Number) o).longValue();

		if ("float".equals(type))
			return ((Number) o).floatValue();

		if ("double".equals(type))
			return ((Number) o).doubleValue();

		if ("String".equals(type))
			return o;

		if ("Byte".equals(type))
			return ((Number) o).byteValue();

		if ("Short".equals(type))
			return ((Number) o).shortValue();

		if ("Character".equals(type))
			return (char) ((Number) o).intValue();

		if ("Integer".equals(type))
			return ((Number) o).intValue();

		if ("Long".equals(type))
			return ((Number) o).longValue();

		if ("Float".equals(type))
			return ((Number) o).floatValue();

		if ("Double".equals(type))
			return ((Number) o).doubleValue();

		throw new IllegalArgumentException("Invalid class name for Configuration Admin data type " + type);
	}

	private static Class getClass(String type) {
		if ("boolean".equals(type))
			return boolean.class;
		if ("Boolean".equals(type))
			return Boolean.class;

		if ("byte".equals(type))
			return byte.class;

		if ("short".equals(type))
			return short.class;

		if ("char".equals(type))
			return char.class;

		if ("int".equals(type))
			return int.class;

		if ("long".equals(type))
			return long.class;

		if ("float".equals(type))
			return float.class;

		if ("double".equals(type))
			return double.class;

		if ("String".equals(type))
			return String.class;

		if ("Byte".equals(type))
			return Byte.class;

		if ("Short".equals(type))
			return Short.class;

		if ("Character".equals(type))
			return Character.class;

		if ("Integer".equals(type))
			return Integer.class;

		if ("Long".equals(type))
			return Long.class;

		if ("Float".equals(type))
			return Float.class;

		if ("Double".equals(type))
			return Double.class;

		throw new IllegalArgumentException("Invalid class name for Configuration Admin data type " + type);
	}
}
