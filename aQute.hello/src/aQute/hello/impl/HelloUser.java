package aQute.hello.impl;

import aQute.bnd.annotation.component.*;
import aQute.service.hello.*;

@Component public class HelloUser {

	
	@Reference(type='*')
	void setHello( HelloService hello ) {
		System.out.println( hello.sayHello());
	}
	
	void unsetHello( HelloService goodbye ) {
		System.out.println( goodbye.sayGoodbye());		
	}
}
