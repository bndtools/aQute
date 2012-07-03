package aQute.service.data;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({
		ElementType.FIELD, ElementType.METHOD
})
public @interface Require {
	Match[] value() default {};
}
