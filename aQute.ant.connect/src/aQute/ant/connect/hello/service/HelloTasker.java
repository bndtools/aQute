package aQute.ant.connect.hello.service;

import aQute.ant.connect.hello.service.HelloTasker.Config;
import aQute.bnd.annotation.component.*;
import aQute.service.ant.*;

@Component(properties = "name=helloService")
public class HelloTasker implements Tasker<Config> {

	public static class Config extends ConnectTask {
		String message = "Hello DS";
		public void setMessage(String m) {
			this.message = m;
		}
	}

	public void execute(final Config task) {
		System.out.println(task.message);
	}

}
