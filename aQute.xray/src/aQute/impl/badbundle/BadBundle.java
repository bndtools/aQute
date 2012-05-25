package aQute.impl.badbundle;

import java.util.*;

import org.osgi.service.cm.*;
import org.osgi.service.log.*;
import org.osgi.service.metatype.*;

import aQute.bnd.annotation.component.*;

@Component
public class BadBundle implements Runnable, ManagedService {

	@Reference
	void setMetaType(MetaTypeService ea) {
		
	}

	public void run() {
		// TODO Auto-generated method stub
		
	}
	
	@Reference
	void setLogService(LogService log) {
		
	}

	public void updated(Dictionary properties) throws ConfigurationException {
		// TODO Auto-generated method stub
		
	}
}
