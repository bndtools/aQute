package aQute.lib.data;

import java.lang.reflect.*;

import aQute.lib.converter.*;

public class structs {
	final static Converter	converter	= new Converter();

	/**
	 * Copy all fields from the from object to the to object where the names
	 * match. This might go faster with the Unsafe object?
	 * 
	 * @param to
	 * @param from
	 * @throws Exception
	 */
	public static void assign(Object from, Object to) throws Exception {
		Field[] ms = to.getClass().getFields();
		for (Field toField : ms) {
			try {
				Field fromField = from.getClass().getField(toField.getName());
				if (toField.getType().isAssignableFrom(fromField.getClass())) {
					toField.set(to, fromField.get(from));
				}
			}
			catch (NoSuchFieldException nsfe) {
				// Ignore
			}
		}
	}

	public static void deep(Object from, Object to) throws Exception {
		// TODO make deep
		Field[] ms = to.getClass().getFields();
		for (Field toField : ms) {
			Field fromField = from.getClass().getField(toField.getName());
			if (toField.getType().isAssignableFrom(fromField.getClass())) {
				toField.set(to, fromField.get(from));
			}
		}
	}

	public static <T> T clone(T from) throws Exception {
		T to = (T) from.getClass().newInstance();
		deep(from, to);
		return to;
	}

}
