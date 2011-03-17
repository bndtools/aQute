package aQute.lib.configurable;

import java.lang.reflect.*;
import java.util.*;
import java.util.regex.*;

public class Converter {
	volatile ClassLoader loader;

	public void setClassLoader(ClassLoader loader) {
		this.loader = loader;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public
	Object convert(Type type, Object o) throws Exception {
		if (type instanceof ParameterizedType) {
			ParameterizedType pType = (ParameterizedType) type;
			return convert(pType, o);
		}

		if (type instanceof GenericArrayType) {
			GenericArrayType gType = (GenericArrayType) type;
			return convertArray(gType.getGenericComponentType(), o);
		}

		Class<?> resultType = (Class<?>) type;

		if (resultType.isArray()) {
			return convertArray(resultType.getComponentType(), o);
		}

		Class<?> actualType = o.getClass();
		if (actualType.isAssignableFrom(resultType))
			return o;

		if (resultType == boolean.class || resultType == Boolean.class) {
			if (Number.class.isAssignableFrom(actualType)) {
				double b = ((Number) o).doubleValue();
				if (b == 0)
					return false;
				else
					return true;
			}
			resultType = Boolean.class;
		} else if (resultType == byte.class || resultType == Byte.class) {
			if (Number.class.isAssignableFrom(actualType))
				return ((Number) o).byteValue();
			resultType = Byte.class;
		} else if (resultType == char.class) {
			resultType = Character.class;
		} else if (resultType == short.class) {
			if (Number.class.isAssignableFrom(actualType))
				return ((Number) o).shortValue();
			resultType = Short.class;
		} else if (resultType == int.class) {
			if (Number.class.isAssignableFrom(actualType))
				return ((Number) o).intValue();
			resultType = Integer.class;
		} else if (resultType == long.class) {
			if (Number.class.isAssignableFrom(actualType))
				return ((Number) o).longValue();
			resultType = Long.class;
		} else if (resultType == float.class) {
			if (Number.class.isAssignableFrom(actualType))
				return ((Number) o).floatValue();
			resultType = Float.class;
		} else if (resultType == double.class) {
			if (Number.class.isAssignableFrom(actualType))
				return ((Number) o).doubleValue();
			resultType = Double.class;
		}

		if (resultType.isPrimitive())
			throw new IllegalArgumentException("Unknown primitive: "
					+ resultType);

		if (Number.class.isAssignableFrom(resultType)
				&& actualType == Boolean.class) {
			Boolean b = (Boolean) o;
			o = b ? "1" : "0";
		} else if (actualType == String.class) {
			String input = (String) o;
			if (Enum.class.isAssignableFrom(resultType)) {
				return Enum.valueOf((Class<Enum>) resultType, input);
			}
			if (resultType == Class.class && loader != null) {
				return loader.loadClass(input);
			}
			if (resultType == Pattern.class) {
				return Pattern.compile(input);
			}
		}

		try {
			Constructor<?> c = resultType.getConstructor(String.class);
			return c.newInstance(o.toString());
		} catch (Throwable t) {
			// handled on next line
		}
		throw new IllegalArgumentException("No conversion to " + resultType
				+ " from " + actualType + " value " + o);
	}

	private Object convert(ParameterizedType pType, Object o)
			throws InstantiationException, IllegalAccessException, Exception {
		Class<?> resultType = (Class<?>) pType.getRawType();
		if (Collection.class.isAssignableFrom(resultType)) {
			Collection<?> input = toCollection(o);
			if (resultType.isInterface()) {
				if (resultType == Collection.class || resultType == List.class)
					resultType = ArrayList.class;
				else if (resultType == Set.class
						|| resultType == SortedSet.class)
					resultType = TreeSet.class;
				else if (resultType == Queue.class || resultType == Deque.class)
					resultType = LinkedList.class;
				else if (resultType == Queue.class || resultType == Deque.class)
					resultType = LinkedList.class;
				else
					throw new IllegalArgumentException(
							"Unknown interface for a collection, no concrete class found: "
									+ resultType);
			}
			@SuppressWarnings("unchecked")
			Collection<Object> result = (Collection<Object>) resultType
					.newInstance();
			Type componentType = pType.getActualTypeArguments()[0];

			for (Object i : input) {
				result.add(convert(componentType, i));
			}
			return result;
		}
		throw new IllegalArgumentException("cannot convert to " + pType
				+ " because it uses generics and is not a Collection");
	}

	Object convertArray(Type componentType, Object o) throws Exception {
		Collection<?> input = toCollection(o);
		Class<?> componentClass = getRawClass(componentType);
		Object array = Array.newInstance(componentClass, input.size());

		int i = 0;
		for (Object next : input) {
			Array.set(array, i++, convert(componentType, next));
		}
		return array;
	}

	private Class<?> getRawClass(Type type) {
		if (type instanceof Class)
			return (Class<?>) type;

		if (type instanceof ParameterizedType)
			return (Class<?>) ((ParameterizedType) type).getRawType();

		throw new IllegalArgumentException(
				"For the raw type, type must be ParamaterizedType or Class but is "
						+ type);
	}

	private Collection<?> toCollection(Object o) {
		if (o instanceof Collection)
			return (Collection<?>) o;

		if (o.getClass().isArray())
			return Arrays.asList((Object[]) o);

		return Arrays.asList(o);
	}

}
