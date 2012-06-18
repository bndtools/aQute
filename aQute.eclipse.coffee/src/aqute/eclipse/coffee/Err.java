package aqute.eclipse.coffee;

import java.util.regex.*;

public class Err {
	final static Pattern	LINE	= Pattern.compile(".*line ([0-9]+).*$",
											Pattern.MULTILINE);
	int						line;
	String					message;

	public Err(String e) {
		this.line = 1;
		this.message = e;
		Matcher m = LINE.matcher(e);
		if (m.find()) {
			this.message = m.group(0);
			this.line = Integer.parseInt(m.group(1)) - 1;
		}
	}

	public int getLine() {
		return line;
	}

	public String getMessage() {
		return message;
	}
}
