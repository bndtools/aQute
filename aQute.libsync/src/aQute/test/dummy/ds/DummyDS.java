package aQute.test.dummy.ds;

import java.lang.reflect.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;

import aQute.bnd.make.component.*;
import aQute.lib.collections.*;
import aQute.lib.osgi.*;

/**
 * Intended to wire a number components for testing
 */
public class DummyDS {
	static Pattern	REFERENCE	= Pattern.compile("([^/]+)/([^/]+)(?:/([^/]+))?");

	public class Reference {
		String	name;
		Method	set;
		Method	unset;
		boolean	multiple;
		boolean	optional;
		boolean	dynamic;
	}

	public class Component<T> {
		Class<T>			type;
		T					instance;
		Map<String,Object>	properties	= new HashMap<String,Object>();
		boolean				wiring;
		Method				activate;
		Method				deactivate;
		List<Reference>		references	= new ArrayList<DummyDS.Reference>();

		T wire(List<Component< ? >> ordered) throws Exception {
			if (instance == null)
				instance = type.newInstance();

			if (ordered.contains(this))
				return instance;

			if (wiring)
				throw new RuntimeException("Cycle " + type);

			wiring = true;

			ClassLoader loader = type.getClassLoader();
			if (loader != null) {
				URL url = loader.getResource(type.getName().replace('.', '/') + ".class");
				Analyzer a = new Analyzer();
				Clazz clazz = new Clazz(a, "", new URLResource(url));
				Map<String,String> d = ComponentAnnotationReader.getDefinition(clazz);
				System.out.println(d);

				for (String key : d.keySet()) {
					if ("activate:".equals(key))
						activate = findMethod(d.get(key));
					else if ("deactivate:".equals(key))
						deactivate = findMethod(d.get(key));
					else {
						Matcher matcher = REFERENCE.matcher(key);
						if (matcher.matches()) {
							Reference r = new Reference();
							r.name = matcher.group(1);
							r.set = findMethod(matcher.group(2));
							r.unset = findMethod(matcher.group(3));

							String type = d.get(key);
							if (type.endsWith("*")) {
								r.multiple = true;
								r.optional = true;
								r.dynamic = true;
							} else if (type.endsWith("?")) {
								r.multiple = false;
								r.optional = true;
								r.dynamic = true;
							} else if (type.endsWith("+")) {
								r.multiple = true;
								r.optional = false;
								r.dynamic = true;
							} else {
								r.multiple = false;
								r.optional = false;
								r.dynamic = false;
							}

							references.add(r);
						}
					}
				}

				for (Reference ref : references) {
					Method m = ref.set;
					Class< ? > requested = m.getParameterTypes()[0];
					List<Component< ? >> refComp = map.get(requested);
					if (refComp == null || refComp.isEmpty()) {
						if (!ref.optional)
							throw new IllegalStateException(type + " requires at least one component for " + ref.name
									+ " of type " + requested);
					} else {
						for (Component< ? > c : refComp) {
							m.setAccessible(true);
							m.invoke(instance, c.wire(ordered));
							if (!ref.multiple)
								break;
						}
					}
				}
				if (activate != null) {
					activate.setAccessible(true);
					Class< ? > types[] = activate.getParameterTypes();
					Object[] parameters = new Object[types.length];
					for (int i = 0; i < types.length; i++) {
						if (Map.class.isAssignableFrom(types[i])) {
							parameters[i] = properties;
						} else
							throw new IllegalArgumentException("Not a pojo, requires " + types[i]);
					}
					activate.invoke(instance, parameters);
				}
			}
			ordered.add(this);
			return instance;
		}

		private Method findMethod(String group) {
			for (Method m : type.getDeclaredMethods())
				if (m.getName().equals(group))
					return m;
			return null;
		}

		public Component<T> $(String key, Object value) {
			properties.put(key, value);
			return this;
		}

		public Component<T> instance(T x) {
			this.instance = x;
			return this;
		}

		private void index(Class< ? > c) {
			while (c != null && c != Object.class) {
				map.add(c, this);
				for (Class< ? > interf : c.getInterfaces()) {
					index(interf);
				}
				c = c.getSuperclass();
			}
		}

	}

	final MultiMap<Class< ? >,Component< ? >>	map			= new MultiMap<Class< ? >,Component< ? >>();
	final Set<Component< ? >>					components	= new HashSet<Component< ? >>();				;
	final List<Component< ? >>					ordered		= new ArrayList<Component< ? >>();				;

	public void wire() throws Exception {
		for (Component< ? > c : components) {
			c.wire(ordered);
		}
	}

	public <T> Component<T> add(Class<T> type) throws Exception {
		Component<T> c = new Component<T>();
		c.type = type;
		map.add(type, c);
		c.index(type);
		components.add(c);
		return c;
	}

	@SuppressWarnings("unchecked")
	public <T> Component<T> add(T instance) throws Exception {
		return add((Class<T>) instance.getClass()).instance(instance);
	}

	public <T> T get(Class<T> c) {
		List<Component< ? >> components = map.get(c);
		if (components == null || components.size() == 0)
			return null;

		return c.cast(components.get(0).instance);
	}
}
