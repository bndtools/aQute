package aQute.flow.impl;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import org.apache.felix.shell.*;
import org.osgi.framework.*;
import org.osgi.service.component.*;

import aQute.bnd.annotation.component.*;
import aQute.service.flow.*;

/**
 * A sample flow command parser for the Felix shell.
 * 
 * The command format is:
 * 
 * -> flow producer ( pipe )* sink
 * 
 */
@Component
public class Flow implements Command {
	final static Pattern MATCH = Pattern
			.compile("([a-zA-Z0-9-_.]+)(\\((.+(,.+)*)\\))?");
	BundleContext context;

	@Activate
	void activate(BundleContext context) {
		this.context = context;
	}

	/**
	 * The execute of the flow command.
	 */
	@Override
	public void execute(String line, PrintStream out, PrintStream err) {
		try {
			String tokens[] = line.split("\\s+");
			parse(tokens, 1);
		} catch (Exception e) {
			e.printStackTrace(err);
		}
	}

	/**
	 * Parse the set of tokens into a flow.
	 * 
	 * @param <A>
	 *            Temporary type var
	 * @param <B>
	 *            Temporary type var
	 * @param tokens
	 *            List of parsed input tokens
	 * @param n
	 *            Index in tokens
	 * @throws InvalidSyntaxException
	 *             if cannot find an element
	 */
	@SuppressWarnings("unchecked")
	<A, B> void parse(String tokens[], int n) throws Exception {

		List<ComponentInstance> instances = new ArrayList<ComponentInstance>();
		try {
			Producer<A> start = find(tokens[n], Producer.class, instances);
			Source<A> rover = start;

			for (int i = n + 1; i < tokens.length - 1; i++) {
				Pipe<A, B> pipe = find(tokens[i], Pipe.class, instances);
				rover.setSink((Sink<A>) pipe);
				rover = pipe;
			}
			Sink<?> end = find(tokens[tokens.length - 1], Sink.class, instances);
			rover.setSink((Sink<A>) end);

			start.produce();
		} finally {
			for (ComponentInstance instance : instances)
				instance.dispose();
		}
	}

	/**
	 * Find and checkout a service.
	 * 
	 * @param funct function name
	 * @param type the desired type
	 * @param list of instances in use 
	 * @return The service object
	 * @throws InvalidSyntaxException
	 */

	private <T> T find(String funct, Class<T> type,
			List<ComponentInstance> instances) throws InvalidSyntaxException {

		Matcher m = MATCH.matcher(funct);
		if (!m.matches())
			throw new IllegalArgumentException("Not a proper element def: "
					+ funct);

		String name = m.group(1);
		String parms = m.group(3);
		Hashtable<String, Object> map = new Hashtable<String, Object>();
		if (parms != null) {
			String p[] = parms.split(",");
			for (int i = 0; i < p.length; i++) {
				map.put(i + "", p[i]);
			}
		}

		String filter = "(component.factory=" + type.getName() + "/" + name
				+ ")";

		ServiceReference refs[] = context.getServiceReferences(
				ComponentFactory.class.getName(), filter);
		if (refs == null || refs.length == 0)
			throw new IllegalStateException("Cannot find the component "
					+ filter);

		ComponentFactory cf = (ComponentFactory) context.getService(refs[0]);

		ComponentInstance instance = cf.newInstance(map);
		instances.add(instance);
		return type.cast(instance.getInstance());
	}

	@Override
	public String getName() {
		return "flow";
	}

	@Override
	public String getShortDescription() {
		return "flow producer ( pipe ) * sink - Create a pipe line";
	}

	@Override
	public String getUsage() {
		return getShortDescription();
	}
}
