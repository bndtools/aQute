package aQute.impl.store.mongo;

import java.net.*;
import java.util.*;

import org.osgi.framework.*;
import org.osgi.service.log.*;

import aQute.bnd.annotation.component.*;
import aQute.bnd.annotation.metatype.Configurable;
import aQute.impl.store.mongo.MongoDBImpl.Config;

import com.mongodb.*;

/**
 * This component is driven by a Managed Service Factory. It opens a Mongo DB,
 * gets a DB object and provides access to the stores. This component implements
 * the aQute.service.store service.
 */
@Component(configurationPolicy = ConfigurationPolicy.require, designateFactory = Config.class, servicefactory = true)
public class MongoDBImpl implements aQute.service.store.DB {
	Mongo		mongo;
	DB			db;
	LogService	log;

	public interface Config {
		/**
		 * The host name or null. If null, the mongo db should be on localhost
		 * and on the default port.
		 * 
		 * @return the host name
		 */
		String host();

		/**
		 * The port number, only used if not 0 and a host is set
		 * 
		 * @return the port number
		 */
		int port();

		/**
		 * The name of the db
		 * 
		 * @return the name of the db
		 */
		String db();

		/**
		 * The name of the db user. If set, a password must also be set.
		 */
		String user();

		/**
		 * The to be used password
		 */
		String _password();
	};

	Config				config;
	ServiceRegistration	gridfs;

	/**
	 * Activate method
	 * 
	 * @throws UnknownHostException
	 * @throws MongoException
	 */
	@Activate
	void activate(Map<String,Object> properties) throws UnknownHostException, MongoException {
		config = Configurable.createConfigurable(Config.class, properties);

		// Get the host
		if (config.host() != null && config.host().length() > 1) {
			if (config.port() != 0)
				mongo = new Mongo(config.host(), config.port());
			else
				mongo = new Mongo(config.host());
		} else
			mongo = new Mongo();

		this.db = mongo.getDB(config.db());

		// Log in if required
		if (config.user() != null && config.user().length() > 1 && config._password() != null) {
			db.authenticate(config.user(), config._password().toCharArray());
		}

	}

	/**
	 * Close the db and unregister the collections
	 */
	@Deactivate
	void deactivate() {
		mongo.close();
	}

	public <T> MongoStoreImpl<T> getStore(Class<T> clazz, String name) throws Exception {
		return new MongoStoreImpl<T>(this, clazz, db.getCollection(name));
	}

	@Reference
	public void setLogService(LogService log) {
		this.log = log;
	}
}
