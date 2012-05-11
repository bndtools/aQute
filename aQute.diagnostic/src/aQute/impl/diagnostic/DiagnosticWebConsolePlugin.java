package aQute.impl.diagnostic;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.felix.webconsole.*;
import org.osgi.framework.*;
import org.osgi.framework.hooks.service.*;
import org.osgi.service.log.*;

import aQute.bnd.annotation.component.*;
import aQute.impl.diagnostic.Data.Bundle.STATE;
import aQute.impl.diagnostic.Data.DiagnosticState;
import aQute.impl.diagnostic.Data.Service;
import aQute.lib.io.*;
import aQute.lib.json.*;

@Component(provide = {Servlet.class, ListenerHook.class}, properties = {"felix.webconsole.label=diagnostics"})
public class DiagnosticWebConsolePlugin extends AbstractWebConsolePlugin implements ListenerHook {
	private static final long	serialVersionUID	= 1L;
	final static JSONCodec		codec				= new JSONCodec();
	BundleContext				context;
	LogReaderService			logreader;
	LogService					log;

	@Activate
	public void activate(BundleContext context) {
		super.activate(context);
		this.context = context;
	}

	@Override
	public String getLabel() {
		return "diagnostics";
	}

	@Override
	public String getTitle() {
		return "Diagnostics";
	}

	public void doGet(HttpServletRequest rq, HttpServletResponse rsp)
			throws ServletException, IOException {

		if (rq.getPathInfo().endsWith("/state.json")) {
			try {
				Data.DiagnosticState state = new DiagnosticState();
				for (Bundle bundle : context.getBundles()) {
					state.bundles.add(data(bundle, state.services));
				}
				rsp.setContentType("application/json");
				String s = codec.enc().put(state).toString();
				rsp.getWriter().append(s).flush();
				
//				log.log(LogService.LOG_ERROR, "testing");
			} catch (Exception e) {
				log.log(LogService.LOG_ERROR, "creating state file", e);
				e.printStackTrace();
			}
		} else
			super.doGet(rq, rsp);
	}

	@Override
	protected void renderContent(HttpServletRequest rq, HttpServletResponse rsp)
			throws ServletException, IOException {
		IO.copy(getClass().getResourceAsStream("main.html"), rsp.getWriter());
	}

	@SuppressWarnings("unchecked")
	private Data.Bundle data(Bundle bundle, List<Service> services) {
		Data.Bundle bd = new Data.Bundle();
		bd.id = bundle.getBundleId();
		bd.bsn = bundle.getSymbolicName();
		bd.name = (String) bundle.getHeaders().get("Bundle-Name");

		bd.handle = bd.bsn == null ? bd.name : bd.bsn;
		if (bd.handle == null)
			bd.handle = bd.id + "";

		if (bd.name.length() > 10) {
			bd.handle = bd.bsn.substring(bd.handle.lastIndexOf('.') + 1);
		}

		switch (bundle.getState()) {
			case Bundle.INSTALLED :
				bd.state = STATE.INSTALLED;
				break;
			case Bundle.RESOLVED :
				bd.state = STATE.RESOLVED;
				break;
			case Bundle.STARTING :
				bd.state = STATE.STARTING;
				break;
			case Bundle.STOPPING :
				bd.state = STATE.STOPPING;
				break;
			case Bundle.UNINSTALLED :
				bd.state = STATE.UNINSTALLED;
				break;
			case Bundle.ACTIVE :
				bd.state = STATE.ACTIVE;
				break;

			default :
				bd.state = STATE.UNKNOWN;
				break;

		}

		if (bundle.getServicesInUse() != null)
			for (ServiceReference ref : bundle.getServicesInUse()) {
				bd.inuse.addAll(data(ref, services));
			}
		if (bundle.getRegisteredServices() != null)
			for (ServiceReference ref : bundle.getRegisteredServices()) {
				bd.registered.addAll(data(ref, services));
			}
		Enumeration<LogEntry> e = logreader.getLog();

		while (e.hasMoreElements()) {
			long now = System.currentTimeMillis();

			LogEntry le = e.nextElement();
			if (le.getBundle() == bundle) {
				if (le.getTime() + TimeUnit.SECONDS.toMillis(120) < now)
					continue;

				Data.LogEntry data = new Data.LogEntry();
				bd.log.add(data);
				data.message = le.getMessage();
				data.time = new Date(le.getTime());
				switch (le.getLevel()) {
					case LogService.LOG_DEBUG :
						data.level = Data.LogEntry.LEVEL.LOG_DEBUG;
						break;
					case LogService.LOG_INFO :
						data.level = Data.LogEntry.LEVEL.LOG_INFO;
						break;
					case LogService.LOG_WARNING :
						data.level = Data.LogEntry.LEVEL.LOG_WARNING;
						break;
					case LogService.LOG_ERROR :
						data.level = Data.LogEntry.LEVEL.LOG_ERROR;
						break;
					default :
						data.level = Data.LogEntry.LEVEL.LOG_OTHER;
				}
				if (bd.alert.compareTo(data.level) < 0)
					bd.alert = data.level;

				if (le.getServiceReference() != null)
					data.service = (Long) le.getServiceReference().getProperty(
							"service.id");

				if (le.getException() != null) {
					data.exceptionMessage = le.getException().getMessage();
					StackTraceElement stes[] = le.getException()
							.getStackTrace();
					StringBuilder sb = new StringBuilder();
					Formatter formatter = new Formatter(sb);

					String cname = null;
					String mname = null;
					for (StackTraceElement ste : stes) {

						formatter.format(
								"%-4d %30s.%s\n",
								ste.getLineNumber(),
								(ste.getClassName().equals(cname) ? "" : ste
										.getClassName()),
								(ste.getMethodName().equals(mname) ? "" : ste
										.getMethodName()));

						cname = ste.getClassName();
						mname = ste.getMethodName();
					}
					data.stackTrace = formatter.toString();
				}
			}

		}
		return bd;
	}

	private List<Service> data(ServiceReference ref, List<Service> services) {
		List<Service> result = new ArrayList<Service>();
		String[] objectClasses = (String[]) ref.getProperty("objectClass");
		for (String objectClass : objectClasses) {
			Data.Service sd = new Data.Service();
			sd.id = (Long) ref.getProperty("service.id");
			sd.objectClass = objectClass;

			sd.registeredBy = ref.getBundle().getBundleId();
			if (ref.getUsingBundles() != null)
				for (Bundle b : ref.getUsingBundles()) {
					sd.usedBy.add(b.getBundleId());
				}
			services.add(sd);
			result.add(sd);
		}
		return result;
	}

	@Override
	public String[] getCssReferences() {
		return new String[] {"/diagnostics/diagnostics.css"};
	}

	public URL getResource(String resource) {
		if (resource.equals("/diagnostics"))
			return null;

		resource = resource.replaceAll("/diagnostics/", "/static/");
		URL url = getClass().getResource(resource);
		return url;
	}

	@Reference
	void setLogReader(LogReaderService log) {
		this.logreader = log;
	}

	@Reference
	void setLog(LogService log) {
		this.log = log;
	}

	public synchronized void added(Collection listeners) {
		for ( Object o : listeners) {
			addListenerInfo( (ListenerInfo) o);
		}
	}

	private void addListenerInfo(ListenerInfo o) {
		System.out.println("added " + o.getFilter());		
	}

	public synchronized void removed(Collection listeners) {
		for ( Object o : listeners) {
			removeListenerInfo( (ListenerInfo) o);
		}
	}

	private void removeListenerInfo(ListenerInfo o) {
		System.out.println("removed " + o.getFilter());		
	}
}