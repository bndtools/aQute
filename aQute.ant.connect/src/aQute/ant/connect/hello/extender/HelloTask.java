package aQute.ant.connect.hello.extender;

import org.apache.tools.ant.*;

public class HelloTask extends Task {
	String message = "Hello Extender";

	public void execute() {
		System.out.println(message);
	}

	
	public void setMessage(String m) {
		this.message = m;
	}
}
