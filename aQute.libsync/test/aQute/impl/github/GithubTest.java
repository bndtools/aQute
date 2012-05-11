package aQute.impl.github;

import java.io.*;
import java.util.*;

import junit.framework.*;
import aQute.lib.hex.*;
import aQute.lib.io.*;
import aQute.libg.cryptography.*;
import aQute.service.github.*;
import aQute.test.dummy.ds.*;
import aQute.test.dummy.log.*;

public class GithubTest extends TestCase {
	DummyDS	ds	= new DummyDS();
	Properties properties= new Properties();

	public void setUp() throws Exception {
		properties.load(new FileInputStream(System.getProperty("user.home")+"/.aws/properties"));
		ds.add(GithubImpl.class);
		ds.add(new DummyLog().direct().stacktrace().full());
		ds.wire();
	}

	public void testSimple() throws Exception {
		Github github = ds.get(Github.class);

		Repository r = github.getRepository("posthooktest").owner("bnd").get();
		Commit commit = r.getCommit("980c4b0293520222d8aa9812e91d9641a7dd88f6");
		assertNotNull(commit);
		Tree tree = r.getTree(commit.tree.sha);
		assertNotNull(tree);
		for ( Entry entry : tree.tree) {
			System.out.println(entry.path);
		}
		assertEquals( "ihello\n", IO.collect(r.getBlob(tree,"test2")));
		
		Digester<SHA1> digester = SHA1.getDigester();
		Entry e = r.getEntry(tree, "test2");
		
		// gits shas are calculated with a header prefix
		String header = String.format("blob %d\u0000", e.size);
		digester.write(header.getBytes());
		IO.copy(r.getBlob(tree, "test2").openStream(), digester);
		assertEquals( e.sha.toLowerCase(), Hex.toHexString(digester.digest().toByteArray()).toLowerCase());
	}


	public void testSecurity() throws Exception {
		DummyDS	ds	= new DummyDS();
		ds.add(GithubImpl.class).$(".secret", properties.getProperty("github.secret")).$("user", properties.getProperty("github.user"));
		ds.add(new DummyLog().direct().stacktrace().full());
		ds.wire();
		
		Repository r = ds.get(Github.class).getRepository("posthooktest").owner("bnd").get();
		Commit commit = r.getCommit("980c4b0293520222d8aa9812e91d9641a7dd88f6");
		assertNotNull(commit);
		
	}
}
