package aQute.javascript.servlet;

public class WrapperClassLoader extends ClassLoader {

	public WrapperClassLoader(ClassLoader classLoader) {
		super(classLoader);
	}

	@Override
	public Class< ? > loadClass(String s, boolean b) throws ClassNotFoundException {
		ClassNotFoundException e = null;
		try {
			return super.loadClass(s, b);
		}
		catch (ClassNotFoundException ee) {
			e = ee;
		}
		System.out.println("Loading " + s + " " + e);
		throw e;
	}
}
