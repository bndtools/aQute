package aQute.lib.configurable;

import java.lang.reflect.*;
import java.util.*;

public class Configurable {

	public static <T> T createConfigurable(Class<T> c,
			Map<String, Object> properties) {
		Object o = Proxy.newProxyInstance(c.getClassLoader(),
				new Class<?>[] { c },
				new ConfigurableHandler(properties, c.getClassLoader()));
		return c.cast(o);
	}
}
