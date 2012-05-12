package aQute.impl.diagnostic;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.felix.webconsole.*;
import org.osgi.framework.*;
import org.osgi.framework.hooks.service.*;
import org.osgi.service.log.*;

import aQute.bnd.annotation.component.*;
import aQute.lib.io.*;
import aQute.lib.json.*;

@Component(provide = {Servlet.class, ListenerHook.class}, properties = {"felix.webconsole.label=diagnostics"})
@SuppressWarnings("rawtypes")
public class DiagnosticWebConsolePlugin extends AbstractWebConsolePlugin
		implements ListenerHook {
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
		if (rq.getPathInfo().endsWith("/stategraph.json")) {
			try {
				StateGraph sg = new StateGraph(context);
				rsp.setContentType("application/json");
				String s = codec.enc().put(sg.build()).toString();
				rsp.getWriter().append(s).flush();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		} else
			super.doGet(rq, rsp);
	}

	@Override
	protected void renderContent(HttpServletRequest rq, HttpServletResponse rsp)
			throws ServletException, IOException {
		IO.copy(getClass().getResourceAsStream("main.html"), rsp.getWriter());
	}

	@Override
	public String[] getCssReferences() {
		return new String[] {"/diagnostics/diagnostics.css"};
	}

	public URL getResource(String resource) {
		if (resource.equals("/diagnostics"))
			return null;

		resource = resource.replaceAll("/diagnostics/", "");
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
		for (Object o : listeners) {
			addListenerInfo((ListenerInfo) o);
		}
	}

	private void addListenerInfo(ListenerInfo o) {
		System.out.println("added " + o.getFilter());
	}

	public synchronized void removed(Collection listeners) {
		for (Object o : listeners) {
			removeListenerInfo((ListenerInfo) o);
		}
	}

	private void removeListenerInfo(ListenerInfo o) {
		System.out.println("removed " + o.getFilter());
	}
}