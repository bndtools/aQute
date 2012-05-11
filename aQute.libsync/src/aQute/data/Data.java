package aQute.data;

import java.lang.reflect.*;
import java.util.*;

import aQute.lib.converter.*;

public class Data {
	final static Converter	converter	= new Converter();

	static class Mismatch {
		public Field	field;
		public String	reason;
		public Object	value;

		public Mismatch(Field f, Object object, String string) {
			this.field = f;
			this.value = object;
			this.reason = string;
		}
	}

	public static List<Mismatch> validate(Object instance) throws Exception {
		List<Mismatch> mismatches = new ArrayList<Mismatch>();

		for (Field f : instance.getClass().getFields()) {
			Match match = f.getAnnotation(Match.class);
			CollectionMatch cmatch = f.getAnnotation(CollectionMatch.class);

			Object value = f.get(instance);
			String reason = null;

			if (match != null) reason = validate(match, value);
			else if (cmatch != null) reason = validate(cmatch, value);

			if (reason != null) mismatches.add(new Mismatch(f, value, reason));
		}
		if (mismatches.isEmpty()) return null;
		else return mismatches;
	}

	public static String validate(Match match, Object field) throws Exception {
		String s = converter.convert(String.class, field);
		if (!s.matches(match.value())) { return "Pattern mismatch, expected "
				+ match.value() + ", but got " + s; }

		if (field != null && field instanceof Number) {
			Number number = (Number) field;
			long l = number.longValue();
			if (l < match.min())
				return "Too small, must be at least " + match.min();
			if (l >= match.max())
				return "Too small, must be at most  " + match.max();
		}
		return null;
	}

	private static String validate(CollectionMatch match, Object value)
			throws Exception {
		if (value instanceof Collection) {
			Collection< ? > coll = (Collection< ? >) value;
			if (coll.size() > match.size())
				return "Collection is too large, max size is " + match.size()
						+ " is now " + coll.size();

			for (Object member : coll) {
				String s = validate(match.value(), member);
				if (s != null) { return "Collection member element mismatch, "
						+ s; }
			}
		}
		if (value.getClass().isArray()) {
			int length = Array.getLength(value);

			if (length > match.size())
				return "Array is too large, max size is " + match.size()
						+ " is now " + length;

			for (int i = 0; i < length; i++) {
				Object member = Array.get(value, i);
				String s = validate(match.value(), member);
				if (s != null) { return "Array member element mismatch, " + s; }
			}

		}
		if (value instanceof Map) {
			Map< ? , ? > map = (Map< ? , ? >) value;
			if (map.size() > match.size())
				return "Map is too large, max size is " + match.size()
						+ " is now " + map.size();

			for (Map.Entry< ? , ? > e : map.entrySet()) {
				String s = validate(match.key(), e.getKey());
				if (s != null) return "Map key mismatch, " + s;
				s = validate(match.value(), e.getValue());
				if (s != null) return "Map key mismatch, " + s;
			}
		}
		return null;
	}

}
