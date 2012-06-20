package org.example.tests;

import junit.framework.*;

import org.osgi.framework.*;

public class ExampleTest extends TestCase {

	private final BundleContext	context	= FrameworkUtil.getBundle(this.getClass()).getBundleContext();

	public void testExample() throws Exception {
		System.out.println("Tested Again!!!!");
	}
}
