package aQute.impl.github;

import java.util.*;

import org.osgi.service.log.*;

import aQute.bnd.annotation.component.*;
import aQute.bnd.annotation.component.Reference;
import aQute.bnd.annotation.metatype.*;
import aQute.service.github.*;

@Component
public class GithubImpl implements Github {
	LogService log;

	interface Cfg {
		String user();
		String _secret();
	}
	Cfg config;
	
	@Activate
	void activate(Map<String,Object> props) {
		this.config = Configurable.createConfigurable(Cfg.class,props);
	}
	
	public RepositoryBuilder getRepository(final String name) {
		return new RepositoryBuilder() {
			String owner;
			String user = config.user();
			String secret = config._secret();
			
			public RepositoryBuilder owner(String owner) {
				this.owner = owner;
				return this;
			}

			public RepositoryBuilder authenticate(String user, String secret) {
				this.user = user;
				this.secret=secret;
				return this;
			}
			public Repository get() {
				return new RepositoryImpl(GithubImpl.this, owner,name, user, secret);
			}
			
		};
	}

	@Reference
	void setLogService(LogService log) {
		this.log = log;
	}
}
