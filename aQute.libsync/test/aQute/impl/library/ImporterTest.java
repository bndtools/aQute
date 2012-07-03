package aQute.impl.library;

import junit.framework.*;
import aQute.impl.metadata.osgi.*;
import aQute.service.library.Library.License;
import aQute.service.library.Library.Revision;

public class ImporterTest extends TestCase {

	public void testSCM() {
		OSGiMetadataParser lii = new OSGiMetadataParser(null, null);
		Revision r = new Revision();
		lii.h2d(r, "scm", "url", "git@github.com:bnd/posthooktest.git", "Bundle-SCM");
		assertTrue(lii.check());
		assertNotNull(r);
		assertNotNull(r.scm);
	}

	public void testLicense() {
		OSGiMetadataParser lii = new OSGiMetadataParser(null, null);
		Revision r = new Revision();
		lii.h2d(r,
				"licenses",
				"name",
				"http://www.opensource.org/licenses/apache2.0.php; description=\"Apache Software License 2.0\"; link=http://www.apache.org/licenses/LICENSE-2.0.html, NAME;description=DESCRIPTION;link=LINK;extra=EXTRA",
				"Bundle-License");
		assertTrue(lii.check());
		assertNotNull(r);
		assertNotNull(r.licenses);
		assertEquals(2, r.licenses.size());

		License lic = r.licenses.get(0);
		assertEquals("http://www.opensource.org/licenses/apache2.0.php", lic.name);
		assertEquals("Apache Software License 2.0", lic.description);
		assertEquals("http://www.apache.org/licenses/LICENSE-2.0.html", lic.link.toString());
		assertNull(lic.__extra);

		lic = r.licenses.get(1);
		assertEquals("NAME", lic.name);
		assertEquals("DESCRIPTION", lic.description);
		assertEquals("LINK", lic.link.toString());
		assertNotNull(lic.__extra);
		assertEquals("EXTRA", lic.__extra.get("extra"));

	}

	public void testDevelopers() {
		OSGiMetadataParser lii = new OSGiMetadataParser(null, null);
		Revision r = new Revision();
		lii.h2d(r,
				"licenses",
				"name",
				"http://www.opensource.org/licenses/apache2.0.php; description=\"Apache Software License 2.0\"; link=http://www.apache.org/licenses/LICENSE-2.0.html",
				"Bundle-License");
		assertTrue(lii.check());
		assertNotNull(r);
		assertNotNull(r.licenses);
		assertEquals(1, r.licenses.size());
	}

}
