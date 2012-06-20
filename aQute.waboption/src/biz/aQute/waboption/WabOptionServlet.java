package biz.aQute.waboption;

import java.io.*;

import javax.servlet.http.*;

public class WabOptionServlet extends HttpServlet {
	private static final long	serialVersionUID	= 1L;

	@Override
	public void doGet(HttpServletRequest rq, HttpServletResponse rsp) throws IOException {
		PrintWriter writer = rsp.getWriter();
		writer.println("<html><body><img src='icon.gif'/>Hello World</body></html>");
	}
}
