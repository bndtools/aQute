package aQute.impl.rest;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.zip.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.osgi.service.http.*;
import org.osgi.service.log.*;

import aQute.bnd.annotation.component.*;
import aQute.bnd.annotation.metatype.*;
import aQute.lib.collections.*;
import aQute.lib.converter.*;
import aQute.lib.json.*;
import aQute.service.rest.*;

@Component(provide = Servlet.class, properties = {"alias=/rest"})
@SuppressWarnings({"unchecked", "rawtypes"})
public class RestServlet extends HttpServlet {
	private static final long	serialVersionUID	= 1L;
	final static Converter		converter			= new Converter();
	final static JSONCodec		codec				= new JSONCodec();
	LogService					log;
	MultiMap<String, Function>	functions			= new MultiMap<String, Function>();

	class Function {
		Method	method;
		Object	target;

		public Function(Object target, Method method) {
			this.target = target;
			this.method = method;
		}
	}

	public void service(HttpServletRequest rq, HttpServletResponse rsp)
			throws IOException {
		String pathInfo = rq.getPathInfo();
		if (pathInfo == null) {
			rsp.getWriter()
					.println(
							"The rest servlet requires that the name of the resource follows the servlet path ('rest'), like /rest/aQute.service.library.Program[/...]*[?...]");
			rsp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		pathInfo = pathInfo.substring(1);
		StringBuilder verb = new StringBuilder(rq.getMethod().toLowerCase());

		List<String> parts = new ExtList<String>(pathInfo.split("/"));

		for (int i = 0; i < parts.size(); i++) {
			String subType = parts.remove(i);
			verb.append(Character.toUpperCase(subType.charAt(0)));
			verb.append(subType, 1, subType.length());
		}

		String acceptEncoding = rq.getHeader("Accept-Encoding");
		boolean deflate = acceptEncoding != null
				&& acceptEncoding.indexOf("deflate") >= 0;

		Map parameters = new HashMap(rq.getParameterMap());

		parameters.put("_request", rq);
		parameters.put("_response", rsp);

		try {
			List<Function> functions = this.functions.get(verb.toString());
			if (functions == null) {
				rsp.getWriter().println(
						"No such rest type found: " + verb
								+ ", available verbs "
								+ this.functions.keySet());
				rsp.setStatus(HttpServletResponse.SC_NOT_FOUND);
				return;

			}
			for (Function f : functions) {

				Object[] args = mapArguments(
						f.method.getGenericParameterTypes(), parameters, parts,
						rq.getInputStream());

				if (args != null) {
					Object result = f.method.invoke(f.target, args);
					if (result == null) {
						rsp.getWriter().println(
								"Cannot " + verb + " Resource " + pathInfo
										+ " with parameters " + parameters
										+ ".");
						rsp.setStatus(HttpServletResponse.SC_NOT_FOUND);
						return;
					}
					rsp.setContentType("application/json;charset=utf-8");
					// TODO test if buffering and then setting length is faster
					OutputStream out = rsp.getOutputStream();
					if (deflate) {
						out = new DeflaterOutputStream(out);
						rsp.setHeader("Content-Encoding", "deflate");
					}
					codec.enc().to(out).put(result).flush();
					return;
				}
			}

			rsp.getWriter().println(
					"Cannot find Resource Manager for uri " + pathInfo + " ("
							+ verb + "), available Resource Managers are "
							+ this.functions.keySet());
			rsp.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return;

		} catch (Exception e) {
			rsp.getWriter().println(
					"Cannot " + verb + " Resource " + pathInfo
							+ " with parameters " + parameters + ".");
			e.printStackTrace(rsp.getWriter());
			rsp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		}
	}

	private Object[] mapArguments(Type[] types, Map parameters,
			List<String> parts, InputStream in) throws Exception {

		// TODO quick checks to see it can never match

		Object[] arguments = new Object[types.length];

		try {
			int i = 0, p = 0;
			if (arguments.length > 0
					&& Options.class.isAssignableFrom((Class) types[0])) {
				arguments[0] = Configurable.createConfigurable(
						(Class) types[0], parameters);
				i++;
			}

			for (; i < arguments.length && p < parts.size(); i++, p++) {
				arguments[i] = converter.convert(types[i], parts.get(p));
			}
			if (p == parts.size()) {
				if (i == arguments.length - 1) {
					arguments[i++] = codec.dec().from(in).charset("UTF-8")
							.get(types[i]);
				}
				if (i == arguments.length)
					return arguments;
			}
		} catch (Exception e) {
			// Ignore since another method might match
		}
		return null;
	}

	@Reference
	void setLog(LogService log) throws ServletException, NamespaceException {
		this.log = log;
	}

	@Reference(type = '*')
	synchronized void addResourceManager(ResourceManager resourceManager) {
		for (Method m : resourceManager.getClass().getMethods()) {
			String name = m.getName();
			if (name.equals("getClass") && m.getParameterTypes().length == 0)
				continue;

			if (name.matches("(get|post|delete|options|put|head|trace)[A-Z].+")) {
				Function f = new Function(resourceManager, m);
				functions.add(name, f);
			}
		}
	}

	synchronized void removeResourceManager(ResourceManager resourceManager) {
		Iterator<Function> i = functions.all();
		while (i.hasNext()) {
			Function f = i.next();
			if (f.target == resourceManager)
				i.remove();
		}
	}
}
