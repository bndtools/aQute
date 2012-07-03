package aQute.impl.configurer;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.*;

import org.osgi.service.cm.*;
import org.osgi.service.coordinator.*;
import org.osgi.service.log.*;

import aQute.bnd.annotation.component.*;
import aQute.lib.converter.*;
import aQute.lib.json.*;
import aQute.libg.sed.*;
import aQute.service.init.*;

@Component(provide = {
		Done.class, Object.class
}, immediate = true)
public class Configurer implements Done {
	public static final JSONCodec	codec			= new JSONCodec();
	public static final Converter	converter		= new Converter();
	static Pattern					PROFILE_PATTERN	= Pattern.compile("\\[([a-zA-Z0-9]+)\\](.*)");

	ConfigurationAdmin				cm;
	LogService						log;
	Coordinator						coordinator;
	String							profile;

	@Activate
	void activate() throws Exception {
		String s = System.getProperty("bnd.configuration");
		if (s == null)
			return;

		s = s.replaceAll("@\\{", "\\${");

		Coordination coordination = null;
		if (coordinator != null)
			coordination = coordinator.begin("Configurer", 5000);
		try {
			final Map<String,String> map = converter.convert(new TypeReference<Map<String,String>>() {},
					System.getProperties());

			File f = new File(System.getProperty("user.home") + File.separator + ".bnd" + File.separator
					+ "local.properties");
			if (f.isFile()) {
				Properties properties = new Properties();
				InputStream in = new FileInputStream(f);
				try {
					properties.load(in);
				}
				finally {
					in.close();
				}
				Map<String,String> ps = converter.convert(new TypeReference<Map<String,String>>() {}, properties);
				map.putAll(ps);
			}

			ReplacerAdapter replacer = new ReplacerAdapter(map);
			s = replacer.process(s);
			if (profile == null)
				profile = map.containsKey("profile") ? map.get("profile") : "debug";

			final List<Hashtable<String,Object>> list = codec.dec().from(s)
					.get(new TypeReference<List<Hashtable<String,Object>>>() {});

			for (Map<String,Object> d : list) {
				Hashtable<String,Object> dictionary = new Hashtable<String,Object>();
				for (Entry<String,Object> e : d.entrySet()) {

					Matcher m = PROFILE_PATTERN.matcher(e.getKey());
					if (m.matches()) {
						String profile = m.group(1);
						if (profile.equals(this.profile))
							dictionary.put(m.group(2), e.getValue());
					} else if (e.getKey().equals(".log")) {
						log.log(LogService.LOG_INFO, converter.convert(String.class, d.get(".log")));
					} else if (e.getKey().equals(".comment"))
						/* Ignore */;
					else {
						dictionary.put(e.getKey(), e.getValue());
					}
				}

				String factory = (String) dictionary.get("service.factoryPid");
				String pid = (String) dictionary.get("service.pid");
				if (pid == null) {
					log.log(LogService.LOG_ERROR, "Invalid configuration, no PID specified: " + dictionary);
					continue;
				}
				dictionary.put("aQute.pid", pid);
				Configuration configuration;

				if (factory != null) {
					// factory configuration
					Configuration instances[] = cm.listConfigurations("(aQute.pid=" + pid + ")");
					if (instances == null) {
						// New factory configuration
						configuration = cm.createFactoryConfiguration(factory, "?");
					} else {
						// Existing factory configuration
						configuration = instances[0];
					}
				} else {
					// normal target configuration
					configuration = cm.getConfiguration(pid, "?");
				}
				// System.out.println("Updating " + dictionary);
				configuration.setBundleLocation(null);
				configuration.update(dictionary);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			if (coordination != null)
				coordination.fail(e);
			log.log(LogService.LOG_ERROR, "While configuring", e);
		}
		finally {
			if (coordination != null)
				coordination.end();
		}
	}

	@Reference
	void setLogService(LogService log) {
		this.log = log;
	}

	@Reference
	void setCM(ConfigurationAdmin cm) {
		this.cm = cm;
	}

	@Reference(type = '?')
	void setCoordinator(Coordinator coord) {
		this.coordinator = coord;
	}

	@Reference(type = '?', target = "(launcher.arguments=*)")
	void setLauncher(Object obj, Map<String,Object> props) {
		String[] args = (String[]) props.get("launcher.arguments");
		for (int i = 0; i < args.length - 1; i++) {
			if (args[i].equals("--profile")) {
				this.profile = args[i++];
				return;
			}
		}
	}
}
