package aQute.data;

public @interface Pattern {
	String[] value();

	String message() default "";
}
