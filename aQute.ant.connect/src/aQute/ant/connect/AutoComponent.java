package aQute.ant.connect;

import java.util.*;

import org.apache.tools.ant.*;

import aQute.bnd.annotation.component.*;
import aQute.service.ant.*;

@Component
public class AutoComponent {
	Project project;
	final Map<String,Class> regs = new HashMap<String, Class>();
	
	@Activate
	void activate() {
		for ( Map.Entry<String, Class> entry : regs.entrySet()) {
			System.out.println("Adding " + entry );
			project.addTaskDefinition(entry.getKey(), entry.getValue());
		}
	}
	
	@Reference
	void setProject( Project project) {
		this.project = project;
	}
	
	@Reference(type='*')
	void addTaskFactory( Tasker<?> tf, Map<String,String> map) {
		Class<?> c = ConnectTask.register(tf);
		this.regs.put(map.get("name"), c);
	}
	void removeTaskFactory( Tasker<?> tf, Map<String,String> map) {
		regs.remove(map.get("name"));
		ConnectTask.unregister(tf);
	}
}
