package aQute.hello;

import aQute.bnd.annotation.component.*;

@Component
public class HelloComponent {

	@Activate
	void activate() {
		System.out.println("Hello World");
	}
	
	@Deactivate
	void deactivate() {
		System.out.println("Goodbye World");
	}
}
