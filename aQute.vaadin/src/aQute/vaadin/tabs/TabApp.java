package aQute.vaadin.tabs;

import java.util.*;

import org.osgi.service.component.*;

import aQute.bnd.annotation.component.*;

import com.vaadin.*;
import com.vaadin.ui.*;
import com.vaadin.ui.Component;

/**
 * Demonstrate how you can use services. This application looks for Component
 * Factory services that have a service property component.factory to
 * com.vaadin.Component/contribution. The first part is a convention to know
 * what classes you are going to create and the second part is a way to not
 * pickup any Component. Declarative services Component Factory services are
 * used because this model maps very well to the instance based model of Vaadin.
 * Though this is a nice demo, in reality you likely have a more elaborate
 * interface that is more specific for the application domain.
 */
@aQute.bnd.annotation.component.Component(factory = "com.vaadin.Application/demo")
public class TabApp extends Application {
	private static final long						serialVersionUID	= 1L;
	final Map<ComponentFactory,ComponentInstance>	mapping				= new IdentityHashMap<ComponentFactory,ComponentInstance>();
	final TabSheet									tabs				= new TabSheet();

	/**
	 * The Vaadin init method, set up the UI.
	 * 
	 * @see com.vaadin.Application#init()
	 */
	@Override
	public void init() {
		setMainWindow(new Window("Module Demo Application", tabs));
		tabs.setSizeFull();
	}

	/**
	 * Receive any contributions. We need to know when to remove the component
	 * so we need a map from the factory to the instance so that when the
	 * factory is unregistered, we can dispose it.
	 * 
	 * @param factory
	 *            The DS Component Factory that can make, well, ehh, components.
	 */
	@Reference(type = '*', target = "(component.factory=com.vaadin.Component/contribution)")
	protected void setContribution(ComponentFactory factory) {
		ComponentInstance ci = factory.newInstance(null);
		Component c = (Component) ci.getInstance();
		synchronized (this) {
			tabs.addTab(c);
			mapping.put(factory, ci);
		}
	}

	/**
	 * The contribution is unregistered.
	 * 
	 * @param factory
	 *            The component factory.
	 */
	protected void unsetContribution(ComponentFactory factory) {
		ComponentInstance ci;
		Component c;
		synchronized (this) {
			ci = mapping.remove(factory);
			c = (Component) ci.getInstance();
			tabs.removeComponent(c);
		}
		c.detach();
		ci.dispose();
	}
}
