package aQute.data.common;

public class CommonPatterns {
	public static final String	BOOLEAN			= "((?i)true|false)";
	public static final String	TEXT			= "[\\w\\d]+";
	public static final String	INTEGER			= "\\d{1,2}";
	public static final String	FLOAT			= INTEGER + "(\\." + INTEGER + "(e|E)(+|-|)" + INTEGER;
	public static final String	QUALIFIER		= ".*";
	public static final String	VERSION			= INTEGER + "(\\." + INTEGER + "(\\." + INTEGER + "((\\.|-)"
														+ QUALIFIER + ")))";

	public static final String	JIDENTIFIER		= "\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*";
	public static final String	QUALIFIED_NAME	= JIDENTIFIER + "(\\." + JIDENTIFIER + ")*";
}
