package aQute.hello.impl;

import aQute.bnd.annotation.component.*;
import aQute.service.hello.*;

@Component(properties = "locale=fr")
public class HelloServiceImpl implements HelloService {

	@Override
	public String sayHello() {
		return "Bonjour";
	}

	@Override
	public String sayGoodbye() {
		return "Au Revoir";
	}

}
