package aQute.service.data;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({
	ElementType.ANNOTATION_TYPE
})
public @interface Match {
	String value();

	String script() default "";

	String reason() default "";
}
