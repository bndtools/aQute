package aQute.service.ant;

import org.apache.tools.ant.*;


public interface Tasker<T extends Task> {
	void execute(T task) throws BuildException;
}
