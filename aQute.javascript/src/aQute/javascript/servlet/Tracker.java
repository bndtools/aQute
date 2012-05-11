package aQute.javascript.servlet;

import java.util.*;

import org.osgi.framework.*;
import org.osgi.util.tracker.*;

class Tracker extends ServiceTracker {
	final List<Object> list = new ArrayList<Object>();
	final boolean dynamic;
	final boolean multiple;
	Object last;
	
	Tracker(BundleContext context, String filter, boolean dynamic, boolean multiple ) throws InvalidSyntaxException {
		super(context, context
				.createFilter(filter), null);
		this.dynamic = dynamic;
		this.multiple = multiple;
	}

}
