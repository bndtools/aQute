package aQute.javascript.servlet;

import java.io.*;
import java.util.regex.*;

import javax.servlet.http.*;

import aQute.lib.io.*;

public class JavascriptServlet extends HttpServlet {

	private static final long	serialVersionUID	= 1L;
	JavascriptComponent			parent;

	public JavascriptServlet(JavascriptComponent parent) throws Exception {
		this.parent = parent;
	}

	Pattern	PATH	= Pattern.compile("/([\\d\\w]+)");

	public void doGet(HttpServletRequest rq, HttpServletResponse rsp)
			throws IOException {
		try {

			String path = rq.getPathInfo();
			Matcher m = PATH.matcher(path);
			if (m.matches())
				getSession(rq.getSession()).invoke(m.group(1), rq, rsp);
			else {
				if (path.equals("/"))
					path = "index.html";
				else
					path = path.substring(1);

				File f = new File(parent.config.resourceDir(), path.replace(
						'/', File.separatorChar));

				if (f.exists()) {
					rsp.setContentLength((int) f.length());
					IO.copy(f, rsp.getOutputStream());
				}
				else
					rsp.sendError(HttpServletResponse.SC_NOT_FOUND);
			}
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	Session	session	= new Session(parent.config);

	private Session getSession(HttpSession session) throws Exception {
		if (!this.session.isStale())
			return (Session) this.session;
		this.session.close();
		this.session = new Session(parent.config);

		return this.session;
	}
}
