package aQute.impl.cafs;

import java.io.*;
import java.util.concurrent.*;
import java.util.zip.*;

import aQute.bnd.annotation.component.*;
import aQute.lib.io.*;
import aQute.lib.json.*;
import aQute.libg.cryptography.*;
import aQute.service.cafs.*;
import aQute.service.cafs.data.*;
import aQute.service.store.*;

/**
 * A content addressable file store. Both leafs (any byte[]) and directories are
 * stored in the database under their SHA1. A directory contains of entries
 * (Entry), where each entry provides a name for a leaf or directory that is
 * below. The digest of the directory is calculated by the canonical JSON
 * encoding of the directory as a CAF object (this is no whitespace, fields
 * sorted). However to take advantage of Mongodb, we store the directories as
 * separate JSON documents.
 */
@Component
public class CAFSImpl implements CAFS {
	static final JSONCodec	codec	= new JSONCodec();
	Store<ContentData>		cafs;

	public CatalogBuilder builder() throws Exception {
		return new CatalogBuilderImpl(this);
	}

	public CatalogImpl getCatalog(byte[] sha) throws Exception {
		InputStream in = retrieve(sha);
		CatalogData cd = codec.dec().from(in).get(CatalogData.class);
		return new CatalogImpl(this, cd);
	}

	public InputStream retrieve(byte[] digest) throws Exception {
		SHA1 sha = new SHA1(digest);
		ContentData cd = new ContentData();
		cd._id = sha.digest();
		cd = cafs.find(cd).one();
		if (cd.file == null)
			return null;

		InflaterInputStream zin = new InflaterInputStream(new FileInputStream(cd.file));
		return zin;
	}

	public byte[] store(InputStream in) throws Exception {
		File tmp = File.createTempFile("cafs", ".tmp");
		try {
			/*
			 * Create a pipe line: in -> SHA1 -> deflater -> md5 -> tmp file
			 */
			FileOutputStream out = new FileOutputStream(tmp);
			try {
				Digester<MD5> md5d = MD5.getDigester(out);
				DeflaterOutputStream zout = new DeflaterOutputStream(md5d);

				Digester<SHA1> sha1d = SHA1.getDigester(zout);
				IO.copy(in, sha1d);
				zout.close(); // make sure output is flushed

				SHA1 sha1 = sha1d.digest();

				/*
				 * The following might actually cause duplicates but we take
				 * that chance. Can be solved by running a process to delete
				 * duplicates
				 */
				ContentData cd = new ContentData();
				cd._id = sha1.digest();

				if (cafs.find(cd).isEmpty()) {
					cd.file = tmp;
					cafs.insert(cd);
				}
				return cd._id;
			}
			finally {
				out.close();
			}
		}
		finally {
			tmp.delete();
		}
	}

	@Reference
	public void setDB(DB db) throws Exception {
		cafs = db.getStore(ContentData.class, "cafs");
	}

	public void clearForTest() throws Exception {
		cafs.all().remove();
	}

	@Override
	public boolean store(byte[] sha, Callable<InputStream> in) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}
}
