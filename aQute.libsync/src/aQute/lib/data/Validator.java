package aQute.lib.data;

import java.lang.reflect.*;
import java.util.*;

import javax.script.*;

import aQute.lib.data.data.Cache;
import aQute.libg.reporter.*;
import aQute.service.data.*;

public class Validator extends ReporterAdapter {
	ScriptEngine	js;

	interface Msgs extends Messages {

		ERROR MissingRequiredValue__(String name, String description);

		ERROR Pattern_Mismatch_(String value, String reason);

		ERROR Script_Fails_(String script, String message);

		ERROR Validate_Script_Fails_(String prefix, String script, String message);

		ERROR Invalid_Reason_(String prefix, String reason);

	}

	Msgs	msgs	= getMessages(Msgs.class);

	public <T> boolean validate(T object, String prefix) throws Exception {
		Cache cache = data.getCache(object.getClass());
		for (Field f : cache.fs) {
			Require req = f.getAnnotation(Require.class);
			Semantics semantics = f.getAnnotation(Semantics.class);
			String description = "";
			if (semantics != null)
				description = semantics.value();

			if (req != null) {
				prefix = prefix + "." + f.getName();

				Object value = f.get(object);
				if (value == null)
					msgs.MissingRequiredValue__(f.getName(), description);
				else {
					String s = null;

					if (Collection.class.isAssignableFrom(f.getType())) {
						Collection< ? > coll = (Collection< ? >) value;
						for (Object o : coll)
							validate(o, prefix);
						s = coll.size() + "";
					} else if (Map.class.isAssignableFrom(f.getType())) {
						Map< ? , ? > map = (Map< ? , ? >) value;
						for (Map.Entry< ? , ? > o : map.entrySet()) {
							validate(o.getKey(), prefix);
							validate(o.getValue(), prefix);
						}
						s = map.size() + "";
					}

					for (Match m : req.value()) {
						if (s == null)
							s = value.toString();

						match(m, s, prefix);
					}
				}
			}
		}
		return isOk();
	}

	private void match(Match m, String s, String prefix) throws ScriptException {
		String reason = m.reason();
		if (!m.value().isEmpty() && !s.matches(m.value())) {
			msgs.Pattern_Mismatch_(m.value(), m.reason());
			return;
		}

		if (!m.script().isEmpty()) {
			if (js == null) {
				js = new ScriptEngineManager().getEngineByName("javascript");
			}
			Bindings bindings = js.getBindings(ScriptContext.ENGINE_SCOPE);
			bindings.put("value", s);
			bindings.put("prefix", prefix);
			bindings.put("validator", this);
			try {
				Object eval = js.eval(m.script());
				if (eval == null || Boolean.FALSE.equals(eval))
					msgs.Invalid_Reason_(prefix, reason);
			}
			catch (Exception e) {
				msgs.Validate_Script_Fails_(prefix, m.script(), e.getMessage());
			}
		}
	}
}
