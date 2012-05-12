package aQute.impl.diagnostic;

import java.util.*;

import aQute.impl.diagnostic.Data.LogEntry.LEVEL;

public interface Data {

	public class DiagnosticState {

		public List<Bundle>		bundles		= new ArrayList<Data.Bundle>();
		public List<Registration>	services	= new ArrayList<Data.Registration>();
	}

	public class Bundle {
		public enum STATE {
			UNKNOWN, INSTALLED, RESOLVED, STARTING, ACTIVE, STOPPING, UNINSTALLED;
		}

		public long				id;
		public String			bsn;
		public String			name;
		public STATE			state;
		public LEVEL			alert		= LEVEL.LOG_OTHER;
		public List<Long>		registered	= new ArrayList<Long>();
		public List<Long>		inuse		= new ArrayList<Long>();
		public List<LogEntry>	log			= new ArrayList<Data.LogEntry>();
		public String			handle;
	}

	public class Registration {
		public long			id;
		public String		name;
		public String[]		objectClasses;
		public String		pid;
		public long			registeredBy;
		public List<Long>	usedBy	= new ArrayList<Long>();
	}

	public class LogEntry {
		public enum LEVEL {
			LOG_OTHER, LOG_INFO, LOG_DEBUG, LOG_WARNING, LOG_ERROR;
		}

		public Date		time;
		public String	message;
		public long		service;
		public String	stackTrace;
		public String	exceptionMessage;
		public LEVEL	level;
	}
}
