package aQute.data;

public @interface CollectionMatch {
	Match key() default @Match();

	Match value();

	int size() default Integer.MAX_VALUE;
}
