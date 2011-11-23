package aQute.ant.connect;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.regex.*;

import org.apache.tools.ant.*;
import org.osgi.framework.*;
import org.osgi.util.tracker.*;

import de.kalpatec.pojosr.framework.launch.*;

public class AutoTask extends Task {
	static Pattern ANTTASK = Pattern
			.compile("\\s*([a-zA-z0-9_]+)*\\s*=*\\s*(.*)\\s*");
	PojoServiceRegistry registry;
	File bundles;

	public void execute() throws BuildException {
		try {
			List<BundleDescriptor> bundles = new ClasspathScanner()
					.scanForBundles();
			Map<String, Object> config = new HashMap<String, Object>();
			config.put(PojoServiceRegistryFactory.BUNDLE_DESCRIPTORS, bundles);

			registry = new de.kalpatec.pojosr.framework.PojoServiceRegistryFactoryImpl()
					.newPojoServiceRegistry(config);

			registry.registerService(Project.class.getName(), getProject(),
					null);

			 for (Bundle bundle : registry.getBundleContext().getBundles()) {
			 System.out.println(bundle.getLocation());
			 }

			ServiceTracker tracker = new ServiceTracker(
					registry.getBundleContext(),
					FrameworkUtil
							.createFilter("(&(objectclass=java.lang.Class)(ant=*))"),
					null) {

				@Override
				public Object addingService(ServiceReference reference) {
					Class<Task> taskClass = (Class<Task>) super
							.addingService(reference);

					AutoTask.this.getProject().addTaskDefinition(
							(String) reference.getProperty("ant"), taskClass);
					return taskClass;
				}

			};
			tracker.open();

			BundleTracker btracker = new BundleTracker(
					registry.getBundleContext(), Bundle.ACTIVE, null) {

				@Override
				public Object addingBundle(Bundle b, BundleEvent event) {
					try {
						String clause = (String) b.getHeaders().get("Ant-Task");
						if (clause != null) {
							Matcher matcher = ANTTASK.matcher(clause);
							if (matcher.matches()) {
								String name = matcher.group(1);
								String cname = matcher.group(2);
								Class<Task> taskClass = (Class<Task>) b
										.loadClass(cname);

								try {
									Field field = taskClass
											.getField("bundleContext");
									field.set(null, registry.getBundleContext());
								} catch (Exception e) {
									//
								}
								AutoTask.this.getProject().addTaskDefinition(
										name, taskClass);
							}
						}
						return null;
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			};
			btracker.open();

			for (ServiceReference ref : registry.getServiceReferences(null,
					null)) {
				System.out.println(Arrays.toString((String[]) ref
						.getProperty("objectclass")));
			}
		} catch (Exception e) {
			throw new BuildException(e);
		}
	}

	public void setBundles(File dir) {
		this.bundles = dir;
	}
}
