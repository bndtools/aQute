package aQute.markdown.main;

import junit.framework.*;

import com.github.rjeschke.txtmark.*;

public class MainTest extends TestCase {

	public void testSimple() throws Exception {
		String s = Processor.process(MainTest.class.getResourceAsStream("test1.md"));
		System.out.println(s);
	}
}
