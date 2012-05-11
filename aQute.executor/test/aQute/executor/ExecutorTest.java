package aQute.executor;

import static org.mockito.Mockito.*;

import java.util.*;

import junit.framework.*;

public class ExecutorTest extends TestCase {

	public void testSimple() {
		ExecutorImpl	ei = new ExecutorImpl();
		Map<String,Object> properties = new HashMap<String,Object>();
		
		ei.activate(properties);
		
		Runnable r = mock(Runnable.class);
		ei.execute(r);
		verify(r).run();
	}
}
