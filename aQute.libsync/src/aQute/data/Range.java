package aQute.data;

public @interface Range {
	double min();
	double max();
	String message() default "";
}
