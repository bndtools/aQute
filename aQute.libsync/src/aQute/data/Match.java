package aQute.data;

public @interface Match {
	String value() default "";
	long min() default Long.MIN_VALUE;
	long max() default Long.MAX_VALUE;
}
