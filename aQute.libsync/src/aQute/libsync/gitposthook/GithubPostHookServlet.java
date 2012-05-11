package aQute.libsync.gitposthook;

import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.osgi.service.http.*;
import org.osgi.service.log.*;

import aQute.bnd.annotation.component.*;
import aQute.lib.hex.*;
import aQute.lib.json.*;
import aQute.libsync.gitposthook.Data.GithubPosthook;
import aQute.libsync.gitposthook.Data.Import;
import aQute.service.task.*;

/**
 * 
 *
 */
@Component(provide = {}, properties={"alias=/github"})
public class GithubPostHookServlet extends HttpServlet {
	private static final long	serialVersionUID	= 1L;
	JSONCodec					codec				= new JSONCodec();
	LogService					log;
	TaskQueue					tasks;

	public void doPost(HttpServletRequest rq, HttpServletResponse rsp) {
		
		try {
			Import imp = new Import();
			imp.ip = rq.getRemoteAddr();
			imp.user = rq.getRemoteUser();
			imp.time = System.currentTimeMillis();
			Reader r = rq.getReader();
			StringBuilder sb = new StringBuilder();
			
			int c;
			while ( (c=r.read()) >= 0) {
				if ( c == '%') {
					char a = (char) r.read();
					char b = (char) r.read();
					c = Hex.nibble(a) * 16 + Hex.nibble(b);
				}
				sb.append((char)c);
			}
			sb.delete(0, 8);
			String s = sb.toString();
			System.out.println("Payload = " + s);
			
			GithubPosthook work = codec.dec().from(s).get(GithubPosthook.class);
			imp.posthook = work;
			
			tasks.with(imp).queue();
			rsp.setStatus(HttpServletResponse.SC_OK);
		} catch (Exception e) {
			log.log(LogService.LOG_ERROR, "Processing git post hook", e);
			rsp.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
		}
	}


	@Reference
	void setTaskQueue(TaskQueue taskQueue) {
		this.tasks = taskQueue;
	}
	
	@Reference
	void setHttp( HttpService http) throws ServletException, NamespaceException {
		http.registerServlet("/github", this, null, null);
	}

	@Reference
	void setLog( LogService log) throws ServletException, NamespaceException {
		this.log = log;
	}
}