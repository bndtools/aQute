package aQute.markdown.model;
import java.io.*;

import aQute.jsite.model.*;
import aQute.lib.io.*;
import aQute.libg.reporter.*;

import junit.framework.*;

public class DocTest extends TestCase {
	public void testDoc() throws Exception {
		ReporterAdapter r = new ReporterAdapter();
		File source = new File("site");
		File target = new File("generated/site");
		
		Site site = new Site(r, source, target);
		IO.delete(target);
		assertTrue( site.prepare());
		
		site.build();
		r.report(System.out);
		assertTrue(r.isOk());
	}
}
