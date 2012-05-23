package aQute.impl.xray;

import java.util.*;

/**
 * The public Data objects used in this package.
 */
public interface Data {

	/**
	 * Holds component information
	 */
	class ComponentDef {
		public long			id;
		public int			index;
		public boolean		unsatisfied;
		public boolean		enabled;
		public String		name;
		public Set<String>	references	= new HashSet<String>();
		public String[]		services;
	}

	/**
	 * Holds Service information
	 */
	class ServiceDef {
		public String			name;
		public int				row			= Integer.MAX_VALUE;
		public int				column;
		public TreeSet<Integer>	registering	= new TreeSet<Integer>();
		public TreeSet<Integer>	getting		= new TreeSet<Integer>();
		public TreeSet<Integer>	listening	= new TreeSet<Integer>();
		public String			shortName;
	}

	/**
	 * Holds Bundle information
	 */
	class BundleDef {
		public enum STATE {
			UNKNOWN, INSTALLED, RESOLVED, STARTING, ACTIVE, STOPPING, UNINSTALLED;
		}

		public long					id;
		public String				bsn;
		public String				name;
		public int					row;
		public STATE				state;
		public List<ComponentDef>	components	= new ArrayList<ComponentDef>();
		public String				log;
		public boolean				errors;
	}

	/**
	 * Holds the Result information
	 */
	class Result {
		public List<ServiceDef>	services	= new ArrayList<ServiceDef>();
		public List<BundleDef>	bundles		= new ArrayList<BundleDef>();
		public String			root;
	}

}
