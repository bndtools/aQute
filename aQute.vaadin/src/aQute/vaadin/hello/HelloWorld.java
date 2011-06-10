package aQute.vaadin.hello;

import com.vaadin.*;
import com.vaadin.ui.*;


/**
 * This is a minimal Hello World App in Vaadin using the {@link VaadinOSGiManager}
 *
 * The http alias is the last part of the factory name.
 */


@aQute.bnd.annotation.component.Component(factory = "com.vaadin.Application/hello")
public class HelloWorld extends Application {
	private static final long serialVersionUID = 1;

	@Override
	public void init() {
		Window window = new Window();
		setMainWindow(window);
		window.addComponent(new Label("Hello Worldx!"));
	}

}
