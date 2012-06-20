package aQute.javascript.servlet;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.Map.Entry;

import javax.servlet.http.*;

import org.mozilla.javascript.*;
import org.osgi.framework.*;

import aQute.javascript.servlet.JavascriptComponent.Config;

public class Session {
	static ThreadLocal<Session>		sessions		= new ThreadLocal<Session>();
	final Config					config;
	long							lastModified;
	final public List<File>			included		= new ArrayList<File>();
	File							file;
	public BundleContext			context;
	public HttpServletRequest		request;
	public HttpServletResponse		response;
	public ScriptableObject			global;
	private List<ServiceReference>	dependencies	= new ArrayList<ServiceReference>();

	static {
		ContextFactory.getGlobal().initApplicationClassLoader(new WrapperClassLoader(Session.class.getClassLoader()));
	}

	Session(Config config) throws Exception {
		this.config = config;
	}

	boolean isStale() {
		for (File f : included)
			if (f.lastModified() > lastModified)
				return true;

		for (ServiceReference ref : dependencies) {
			if (ref.getBundle() == null)
				return true;
		}

		return false;
	}

	public void include(InputStream in) throws IOException {
		Context ctx = Context.getCurrentContext();
		ctx.evaluateReader(global, new InputStreamReader(in), "<in>", -1, null);
	}

	public void incl(String o) throws Exception {
		String path = o.toString();
		File f = new File(config.scriptDir(), path.replace('/', File.separatorChar));
		if (f.isFile()) {

			include(new FileInputStream(f));
			if (f.lastModified() > lastModified)
				lastModified = f.lastModified();
			included.add(f);
		} else
			throw new IllegalArgumentException("No such included file: " + path + " in " + config.scriptDir());
	}

	private void put(Scriptable scope, String name, Object x) {
		Object o = Context.javaToJS(x, scope);
		ScriptableObject.putProperty(scope, name, o);
	}

	public void invoke(String name, HttpServletRequest rq, HttpServletResponse rsp) throws Exception {
		Context ctx = Context.enter();

		try {

			synchronized (this) {
				if (global == null) {
					global = ctx.initStandardObjects();
					global.defineFunctionProperties(new String[] {
							"println", "json", "service"
					}, getClass(), ScriptableObject.DONTENUM);
					put(global, "E", this);
					include(getClass().getResourceAsStream("base.js"));
					incl("server.jss");
				}
			}

			Scriptable requestScope = ctx.newObject(global);
			requestScope.setPrototype(global);
			requestScope.setParentScope(null);

			put(requestScope, "request", rq);
			put(requestScope, "response", rsp);

			StringWriter sw = new StringWriter();
			try {
				Scriptable args = ctx.newObject(requestScope);

				for (Object key : rq.getParameterMap().keySet()) {
					Object o = (Object[]) rq.getParameterMap().get(key);
					if (o.getClass().isArray() && Array.getLength(o) == 1)
						o = Array.get(o, 0);

					ScriptableObject.putProperty(args, (String) key, Context.javaToJS(o, requestScope));
				}
				Function function = (Function) requestScope.get(name, requestScope);

				Object o = function.call(ctx, requestScope, requestScope, new Object[] {
						this, args
				});
				if (o instanceof Undefined)
					return;

				json(o, rsp.getWriter(), null);
			}
			finally {
				String s = sw.toString();
				if (!s.isEmpty())
					rsp.getWriter().println("Errors: " + s);
			}
		}
		finally {
			Context.exit();
		}
	}

	private void json(Object o, Appendable app, IdentityHashMap<Object,Object> set) throws IOException {

		if (o == null) {
			app.append("null");
			return;
		}

		if (o instanceof String) {
			string(o.toString(), app);
			return;
		}

		if (o instanceof Number) {

			// Double show up while they are actually ints. So
			// clean up
			if (o instanceof Double) {
				double d = (Double) o;
				if (d < Long.MAX_VALUE) {
					double r = Math.rint(d);
					if (d == r)
						o = (long) d;
				}
			}
			app.append(o.toString());
			return;
		}

		if (set == null)
			set = new IdentityHashMap<Object,Object>();
		else if (set.containsKey(o))
			throw new IllegalStateException("cycle detected in JSON");
		else
			set.put(o, o);

		if (o.getClass().isArray()) {
			int length = Array.getLength(o);
			app.append("[");
			String del = "";
			for (int i = 0; i < length; i++) {
				app.append(del);
				json(Array.get(o, i), app, set);
				del = ",";
			}
			app.append("]");
			return;
		}

		if (o instanceof NativeArray) {
			NativeArray na = (NativeArray) o;
			long length = na.getLength();
			app.append("[");
			String del = "";
			for (long i = 0; i < length; i++) {
				app.append(del);
				json(na.get(i), app, set);
				del = ",";
			}
			app.append("]");
			return;
		}
		if (o instanceof NativeObject) {
			NativeObject no = (NativeObject) o;
			app.append("{");
			String del = "";
			for (Entry<Object,Object> es : no.entrySet()) {
				app.append(del);
				String key = es.getKey().toString();
				Object value = es.getValue();
				string(key, app);
				app.append(':');
				json(value, app, set);
				del = ",";
			}
			app.append("}");
			return;
		}

		string(Context.toString(o.toString()), app);
		return;
	}

	private void string(String s, Appendable app) throws IOException {
		app.append('"');
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			switch (c) {
				case '"' :
					app.append("\\\"");
					break;

				case '\\' :
					app.append("\\\\");
					break;

				case '\b' :
					app.append("\\b");
					break;

				case '\f' :
					app.append("\\f");
					break;

				case '\n' :
					app.append("\\n");
					break;

				case '\r' :
					app.append("\\r");
					break;

				case '\t' :
					app.append("\\t");
					break;

				default :
					if (Character.isISOControl(c)) {
						app.append("\\u");
						app.append("0123456789ABCDEF".charAt(0xF & (c >> 12)));
						app.append("0123456789ABCDEF".charAt(0xF & (c >> 8)));
						app.append("0123456789ABCDEF".charAt(0xF & (c >> 4)));
						app.append("0123456789ABCDEF".charAt(0xF & (c >> 0)));
					} else
						app.append(c);
			}
		}
		app.append('"');
	}

	public static Object include(Context cx, Scriptable scope, Object[] args, Function funObj) throws Exception {
		Session s = sessions.get();
		s.incl(args[0].toString());
		return Context.getUndefinedValue();
	}

	public static Object println(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
		PrintStream writer = System.out;
		for (int i = 0; i < args.length; i++) {
			if (i > 0)
				writer.print(" ");
			String s = Context.toString(args[i]);
			writer.print(s);
		}
		writer.println();
		return Context.getUndefinedValue();
	}

	public static Object service(Context cx, Scriptable scope, Object[] args, Function funObj)
			throws InvalidSyntaxException {
		Session s = sessions.get();

		Object o = args[0];
		if (o instanceof NativeJavaClass) {
			Class< ? > clazz = ((NativeJavaClass) o).getClassObject();
			return s.service(clazz);
		}
		return Context.getUndefinedValue();
	}

	private Object service(Class< ? > clazz) {
		ServiceReference ref = context.getServiceReference(clazz.getName());
		if (ref == null)
			return null;

		Object o = context.getService(ref);
		if (o != null)
			dependencies.add(ref);
		return Context.javaToJS(o, global);
	}

	public void close() {
		for (ServiceReference ref : dependencies)
			context.ungetService(ref);
		dependencies = null;
	}

}
