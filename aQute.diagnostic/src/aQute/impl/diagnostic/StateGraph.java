package aQute.impl.diagnostic;

import java.util.*;

import org.osgi.framework.*;

import aQute.impl.diagnostic.Data.Bundle.STATE;
import aQute.impl.diagnostic.Data.Registration;
import aQute.lib.collections.*;

public class StateGraph extends Graph {
	final Map<String, Service>		services	= new HashMap<String, Service>();
	final MultiMap<Long, Service>	index		= new MultiMap<Long, Service>();
	final BundleContext				context;

	class Service {
		String	objectClass;
		Node	port;
		Link	link;
	}

	public StateGraph() {
		context = null;
	}

	StateGraph(BundleContext context) {
		this.context = context;
	}

	/**
	 * Build up the graph
	 * 
	 * @throws InvalidSyntaxException
	 */
	StateGraph build() throws InvalidSyntaxException {
		for (ServiceReference ref : context.getServiceReferences(null, null)) {
			Data.Registration service = data(ref);
			for (String oc : service.objectClasses) {
				Service s = services.get(oc);
				if (s == null) {
					s = new Service();
					s.objectClass = oc;

					s.port = addNode(oc, null);
					s.port.type = "get";
					s.port.name = from(20, oc);
					s.port.title = oc;
					s.port.state = "registered";
					services.put(oc, s);
				}
				index.add(service.id, s);
			}
		}

		Bundle[] bundles = context.getBundles();
		int n = bundles.length-1;
		int selected = 0;
		int current = 0;
		
		for (Bundle bundle : bundles) {
			Data.Bundle data = data(bundle);

			Node node = addNode("b" + data.id, data);
			node.name = from(15, data.bsn, data.name, "" + data.id);
			node.title = data.bsn;
			node.state = data.state.toString();
			node.type = "bundle";
			if ( false ) {//bundle == bundles[selected] ) {
				node.x = 500;
				node.y = 500;
				node.fixed = true;
			} else {
				node.x = (int) (Math.sin( 2 * current * Math.PI/n)*400 + 500);
				node.y = (int) (Math.cos( 2 * current * Math.PI/n)*400 + 500);
				node.fixed = true;
				current++;
			}
			for (long serviceid : data.inuse) {
				for (Service s : index.get(serviceid)) {
					link(node, s.port);
				}
			}
			for (long serviceid : data.registered) {
				for (Service s : index.get(serviceid)) {
					link(node, s.port);
				}
			}
		}
		return this;
	}

	String from(int n, String... strings) {
		for (String s : strings) {
			if (s != null) {
				if (s.length() > n) {
					s = s.substring(s.lastIndexOf('.') + 1);
					if (s.length() > n) {
						s = s.substring(s.lastIndexOf('.') + 1);
						if (s.length() > n) {
							s = "..." + s.substring(s.length() - 10);
						}
					}
				}
				return s;
			}
		}
		return "<>";
	}

	private Data.Bundle data(Bundle bundle) {
		Data.Bundle bd = new Data.Bundle();
		bd.id = bundle.getBundleId();
		bd.bsn = bundle.getSymbolicName();
		bd.name = (String) bundle.getHeaders().get("Bundle-Name");

		switch (bundle.getState()) {
			case Bundle.INSTALLED :
				bd.state = STATE.INSTALLED;
				break;
			case Bundle.RESOLVED :
				bd.state = STATE.RESOLVED;
				break;
			case Bundle.STARTING :
				bd.state = STATE.STARTING;
				break;
			case Bundle.STOPPING :
				bd.state = STATE.STOPPING;
				break;
			case Bundle.UNINSTALLED :
				bd.state = STATE.UNINSTALLED;
				break;
			case Bundle.ACTIVE :
				bd.state = STATE.ACTIVE;
				break;

			default :
				bd.state = STATE.UNKNOWN;
				break;
		}

		if (bundle.getServicesInUse() != null)
			for (ServiceReference ref : bundle.getServicesInUse()) {
				bd.inuse.add((Long) ref.getProperty("service.id"));
			}
		if (bundle.getRegisteredServices() != null)
			for (ServiceReference ref : bundle.getRegisteredServices()) {
				bd.registered.add((Long) ref.getProperty("service.id"));
			}

		return bd;
	}

	private Registration data(ServiceReference ref) {
		Data.Registration sd = new Data.Registration();
		sd.objectClasses = (String[]) ref.getProperty("objectClass");
		sd.id = (Long) ref.getProperty("service.id");
		sd.name = sd.objectClasses[0];
		sd.registeredBy = ref.getBundle().getBundleId();
		if (ref.getUsingBundles() != null) {
			for (Bundle b : ref.getUsingBundles()) {
				sd.usedBy.add(b.getBundleId());
			}
		}
		return sd;
	}

}
