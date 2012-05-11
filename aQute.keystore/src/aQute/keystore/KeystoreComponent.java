package aQute.keystore;

import java.io.*;
import java.util.*;
import java.util.logging.*;

import org.osgi.service.log.*;
import aQute.osgi.log.*;

import aQute.bnd.annotation.component.*;
import aQute.bnd.annotation.metatype.*;

@Component(configurationPolicy = ConfigurationPolicy.require)
public class KeystoreComponent {
	static File	cwd			= new File(System.getProperty("user.dir"));
	static File	keystores	= new File(cwd, "keystores");
	static {
		keystores.mkdirs();
	}

	interface Log {
		@Format("Failed to store KeyStore %s, exception %s")
		ERROR failedToStoreKeyStore(String name, Throwable throwable);

		INFO activating(String name);
	}

//	Log	log = Logger.newInstance(Log.class, "Key Store Component");
	interface Config {
		
		String name();

		byte[] data();

		long time();
	};

	Config	config;

	@Activate
	void activate(Map<String, Object> properties) {
		this.config = Configurable.createConfigurable(Config.class, properties);
		File out = new File(keystores, config.name() + ".tmp");
		File to = new File(keystores, config.name());
		try {
			FileOutputStream fout = new FileOutputStream(out);
			try {
				fout.write(config.data());
				fout.close();
				out.renameTo(to);
			}
			finally {
				fout.close();
			}
		}
		catch (Throwable e) {
			out.delete();
//			log.failedToStoreKeyStore$At$(config.name(), to, e);
		}
	}

	@Reference(type='*')
	public void setLog(LogService log) {
//		log.setLogService(log);
	}

}
