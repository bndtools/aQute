package aQute.service.data;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({
		ElementType.FIELD, ElementType.METHOD
})
public @interface Semantics {
	String value();
}
