package aQute.speak.gossip;

import org.osgi.service.log.*;

import aQute.bnd.annotation.component.*;
import aQute.service.speak.*;

@Component(immediate=true)
public class Gossip implements LogListener {
	Speak speak;
	LogListener listener;

	@Reference
	protected void setLogReader(LogReaderService lr) {
		lr.addLogListener(this);
	}

	void unsetLogReader(LogReaderService lr) {
		lr.removeLogListener(this);
	}

	public void logged(LogEntry entry) {
		try {
			speak.say(entry.getMessage());
		} catch (Exception e) {
			// Ignore
		}
	}

	@Reference
	protected void setSpeak(Speak speak) {
		this.speak = speak;
	}
}
