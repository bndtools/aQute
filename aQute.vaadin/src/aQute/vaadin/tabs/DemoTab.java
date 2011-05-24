package aQute.vaadin.tabs;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;

@aQute.bnd.annotation.component.Component(factory = "com.vaadin.Component/contribution", properties = "name"
		+ "=hello")
public class DemoTab extends CustomComponent {
	private static final long serialVersionUID = 1L;

	public DemoTab() {
		setCaption("Demo");
		setCompositionRoot(new Label("Hello, this is Module 1"));
	}

}
