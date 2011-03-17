package aQute.lib.configurable;

import java.lang.reflect.*;
import java.util.*;

class ConfigurableHandler implements InvocationHandler {
	final Converter converter;
	final Map<String, Object> properties;
	ConfigurableHandler(Map<String, Object> properties, ClassLoader loader) {
		this.properties = properties;
		this.converter = new Converter();
		this.converter.setClassLoader(loader);
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		Meta.AD ad = method.getAnnotation(Meta.AD.class);
		String id = ad.id();
		if ( id == Meta.NULL)
			id = method.getName();
		
		Object o = properties.get(id);
		if ( o == null ) 
			o = properties.get( id.replace('_', '.'));
		
		if ( o == null && id.startsWith("_")) 
			o = properties.get( id.substring(1));
		
		if (o == null) {
			if ( ad.required())
				throw new IllegalStateException("Attribute is required but not set " + method.getName());

			o = ad.deflt();
			if (o.equals(Meta.NULL))
				return null;
		}

		return converter.convert( method.getGenericReturnType(), o);
	}

}
