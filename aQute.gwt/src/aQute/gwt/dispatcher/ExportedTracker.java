package aQute.gwt.dispatcher;

import java.util.*;

import org.osgi.framework.*;
import org.osgi.util.tracker.*;

/**
 * This class tracks exported services and puts the highest priority service for
 * a given class into a map.
 * 
 */
class ExportedTracker extends ServiceTracker {
	// Maintains the priority order for each interface -> service reference. The
	// first ref is the highest priority
	final Map<String, SortedSet<ServiceReference>> priority = new HashMap<String, SortedSet<ServiceReference>>();

	// The map that is updated. The code synchronizes on this map for changes.
	final Map<String, Class<?>> rpcMap;

	ExportedTracker(BundleContext context, HashMap<String, Class<?>> rpcMap)
			throws InvalidSyntaxException {
		super(context, context.createFilter("(service.exported.interfaces=*)"),
				null);
		this.rpcMap = rpcMap;
	}

	/**
	 * For each object class, add the service to the priority and if the highest
	 * priority then set it in the rpc map..
	 */
	@Override
	public Object addingService(ServiceReference ref) {
		String[] objectclass = (String[]) ref
				.getProperty(Constants.OBJECTCLASS);
		for (String name : objectclass) {
			set(name, ref);
		}
		return ref;
	}

	/**
	 * Remove the reference and if necessary update the rpc map.
	 */
	@Override
	public void removedService(ServiceReference ref, Object s) {
		String[] objectclass = (String[]) ref
				.getProperty(Constants.OBJECTCLASS);
		for (String name : objectclass) {
			unset(name, ref);
		}
	}

	/**
	 * Adds the reference as a server for the service name and the calculates
	 * the priority and ensures that the highest priority service reference is
	 * used in the map.
	 * 
	 * @param name
	 *            the name of the interface
	 * @param ref
	 *            the service reference that exports this interface
	 */
	private void set(String name, ServiceReference ref) {
		synchronized (rpcMap) {
			SortedSet<ServiceReference> refs = priority.get(name);
			if (refs == null) {
				refs = new TreeSet<ServiceReference>(Collections.reverseOrder());
				priority.put(name, refs);
			}
			refs.add(ref);
			if (refs.first() == ref)
				select(name, ref);
		}
	}

	/**
	 * Removes the reference as a service and calculates the priority and
	 * ensures that the highest priority service reference is used in the map.
	 * 
	 * @param name
	 *            the name of the interface
	 * @param ref
	 *            the service reference that exports this interface
	 */
	private void unset(String name, ServiceReference ref) {
		synchronized (rpcMap) {
			SortedSet<ServiceReference> refs = priority.get(name);
			if (refs == null || refs.isEmpty()) {
				// should not happen
				System.out.println("Unexpectedly found no refs for " + name);
				return;
			}

			boolean first = refs.first() == ref;
			refs.remove(ref);

			if (first) {
				if (refs.isEmpty()) {
					rpcMap.remove(name);
					priority.remove(name);
				} else
					select(name, refs.first());
			}
		}
	}

	/**
	 * Selected reference for a name.
	 * 
	 * @param name the interface name
	 * @param ref the selected reference
	 */
	private void select(String name, ServiceReference ref) {
		try {
			Class<?> c = ref.getBundle().loadClass(name);
			rpcMap.put(name, c);
		} catch (Exception e) {
			// Ignore because the bundle should have access to its interface
			e.printStackTrace();
		}
	}
}
