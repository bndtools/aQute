package aQute.osgi.log;

import java.io.*;
import java.lang.reflect.*;

import org.osgi.framework.*;
import org.osgi.service.log.*;

public class Logger implements InvocationHandler {
	LogService			log;
	volatile boolean	on;
	String				type;
	int					level;

	public Logger(String type) {
		this.type = type;
	}

	@SuppressWarnings("unchecked")
	static <T> T newInstance(Class<T> clazz, String type) {
		return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class< ? >[] {
			clazz
		}, new Logger(type));
	}

	public final Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if (on) {
			switch (level) {
				case LogService.LOG_DEBUG :
				case LogService.LOG_INFO :
				case LogService.LOG_WARNING :
				case LogService.LOG_ERROR :
			}
			if (method.getReturnType() == DEBUG.class)
				log(LogService.LOG_DEBUG, method, args);
			else if (method.getReturnType() == INFO.class)
				log(LogService.LOG_INFO, method, args);
			else if (method.getReturnType() == WARNING.class)
				log(LogService.LOG_WARNING, method, args);
			else if (method.getReturnType() == ERROR.class)
				log(LogService.LOG_ERROR, method, args);
		}
		return null;
	}

	void log(int level, Method method, Object args[]) throws IOException {

		ServiceReference< ? > ref = null;
		Throwable t = null;
		int call = 0;

		for (int i = 0; i < args.length; i++) {
			if (args[i] == null)
				continue;

			if (args[i] instanceof ServiceReference && ref != null) {
				ref = (ServiceReference< ? >) args[i];
				call += 1;
			}
			if (args[i] instanceof Throwable && t != null) {
				call += 2;
				t = (Throwable) args[i];
			}
		}

		String message;
		Format format = method.getAnnotation(Format.class);
		if (format != null) {
			message = String.format(format.value(), args);
		} else {
			StringBuilder sb = new StringBuilder();
			StringReader sr = new StringReader(method.getName());
			int c;
			int i = 0;
			while ((c = sr.read()) > 0) {
				char cc = (char) c;
				if (Character.isUpperCase(cc)) {
					sb.append(" ");
					cc = Character.toLowerCase(cc);
				} else if (cc == '$') {
					if (i < args.length)
						sb.append(args[i++]);
					else
						sb.append("<missing parameter>");
				}
				sb.append(cc);
			}
			String del = ": ";
			while (i < args.length) {
				sb.append(del);
				sb.append(args[i++]);
				del = " ";
			}

			message = sb.toString();
		}

		switch (call) {
			case 0 :
				log.log(level, message);
				break;
			case 1 :
				log.log(ref, level, message);
				break;

			case 2 :
				log.log(level, message, t);
				break;
			case 3 :
				log.log(ref, level, message, t);
				break;
		}

	}

	public static void setLogService(Object proxy, LogService logService) {
		InvocationHandler h = Proxy.getInvocationHandler(proxy);
		if (h != null && h instanceof Logger) {
			((Logger) h).setLogService0(logService);
		}
	}

	private void setLogService0(LogService logService) {
		if (logService == null)
			on = false;

		this.log = logService;
		if (logService != null)
			on = true;

	}

	public static void setLevel(Object proxy, int level) {
		InvocationHandler h = Proxy.getInvocationHandler(proxy);
		if (h != null && h instanceof Logger) {
			((Logger) h).setLevel0(level);
		}
	}

	private void setLevel0(int level) {
		this.level = level;
	}
}
