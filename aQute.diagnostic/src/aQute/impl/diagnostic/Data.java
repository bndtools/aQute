package aQute.impl.diagnostic;

import java.util.*;

import aQute.impl.diagnostic.Data.LogEntry.LEVEL;

public interface Data {

	public class DiagnosticState {
		public List<Bundle>		bundles		= new ArrayList<Data.Bundle>();
		public List<Service>	services	= new ArrayList<Data.Service>();
	}

	public class Bundle {
		public enum STATE {
			UNKNOWN, INSTALLED, RESOLVED, STARTING, ACTIVE, STOPPING, UNINSTALLED;
		}

		public long				id;
		public String			bsn;
		public String			name;
		public STATE			state;
		public LEVEL			alert = LEVEL.LOG_OTHER;
		public List<Service>	registered	= new ArrayList<Data.Service>();
		public List<Service>	inuse		= new ArrayList<Data.Service>();
		public List<LogEntry>	log			= new ArrayList<Data.LogEntry>();
		public String	handle;
	}

	public class Service {
		public long			id;
		public String		objectClass;
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
