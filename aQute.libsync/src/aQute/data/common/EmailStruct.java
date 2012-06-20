package aQute.data.common;

import aQute.data.*;

public class EmailStruct {

	@Pattern("/.+@.+\\.[a-z]{2,4}/.match(${@})")
	public String	email;
	public String	name;
}
