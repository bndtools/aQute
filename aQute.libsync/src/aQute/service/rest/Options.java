package aQute.service.rest;

import javax.servlet.http.*;

public interface Options {
	HttpServletRequest _request();

	HttpServletResponse _response();
}
