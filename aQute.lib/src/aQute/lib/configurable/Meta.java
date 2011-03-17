package aQute.lib.configurable;

import java.lang.annotation.*;
import java.util.*;

/**
 * The Meta interface provides access to the properties that underly a PID
 * interface and their delta status. Any PID interface can implement this
 * interface. This interface will not count as a PID interface, that is, its PID
 * will not map to a configuration record. Implementing this interface is
 * optional.
 * 
 * @ConsumerInterface
 */

public interface Meta {
	/**
	 * Return the properties for a callback to the
	 * {@link Configurable#setup(Object)} method. The contents are the merged
	 * contents of all involved PIDs. The returned properties are not
	 * modifiable. The map is case insensitive.
	 * 
	 * @return the merged properties
	 */
	Map<String, ? > getProperties();

	final String NULL = "¤NULL¤";
	
	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	@interface OCD {
		String name() default NULL;
		String pid() default NULL;
		boolean factory() default false;
	}
	
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	@interface AD {
		String name() default NULL;
		String id() default NULL;
		String type() default "String";
		int cardinality() default 0;
		String min() default NULL;
		String max() default NULL;
		String deflt() default NULL;
		boolean required() default false;
		String [] optionLabels() default NULL;
		String [] optionValues() default NULL;
	}
}
