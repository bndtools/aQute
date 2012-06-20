package aQute.impl.cafs;

import java.io.*;
import java.security.*;
import java.util.*;

import aQute.lib.io.*;
import aQute.libg.cryptography.*;
import aQute.service.cafs.*;

public class CatalogBuilderImpl implements CatalogBuilder, Closeable {
	final CAFSImpl			cafs;
	final Node				root	= new Node();
	final File				tmp;
	final FileOutputStream	fout;

	CatalogBuilderImpl(CAFSImpl cafs) throws IOException {
		this.cafs = cafs;
		tmp = File.createTempFile("shacalc", ".tmp");
		fout = new FileOutputStream(tmp);
	}

	public Catalog build() throws Exception {

		fout.close();

		final RandomAccessFile raf = new RandomAccessFile(tmp, "r");
		try {
			Map<SHA1,Node> nodes = new HashMap<SHA1,Node>();

			/*
			 * Calculate the digests for the catalog nodes
			 */
			root.tree(0);
			root.fixupCatalogs(nodes);

			/*
			 * Find out what SHAs we're missing
			 */

			for (ContentData cd : cafs.cafs.select("_id").in("_id", nodes.keySet()))
				nodes.remove(new SHA1(cd._id));

			for (final Node f : nodes.values()) {
				if (f.catalogData != null) {
					byte[] sha = cafs.store(new ByteArrayInputStream(f.catalogData));
					assert new SHA1(sha).equals(f.digest);
				} else {
					raf.seek(f.pos);
					InputStream in = new InputStream() {
						int	size	= f.size;

						@Override
						public int read(byte[] data, int off, int len) throws IOException {
							len = Math.min(size, len);
							int read = raf.read(data, off, len);
							if (read != -1) {
								size -= read;
							} else {
								assert size == 0;
							}

							return read;
						}

						@Override
						public int read() throws IOException {
							if (size < 0)
								return -1;
							size--;
							return raf.read();
						}
					};
					byte[] SHA1 = cafs.store(in);
					assert new SHA1(SHA1).equals(f.digest);
				}
			}

			return new CatalogImpl(cafs, root.getCatalogData());
		}
		finally {
			raf.close();
			close();
		}
	}

	public CatalogBuilder add(String path, InputStream in) throws Exception {
		Node entry = root.getNode(path);

		long pos = fout.getChannel().position();
		SHA1 digest = sha(in, fout);
		int size = (int) (fout.getChannel().position() - pos);

		entry.digest = digest;
		entry.size = size;
		entry.pos = pos;
		return this;
	}

	public CatalogBuilder setTime(String path, long time) throws Exception {
		Node entry = root.getNode(path);
		entry.time = time;
		return this;
	}

	public CatalogBuilder setComment(String path, String comment) throws Exception {
		Node entry = root.getNode(path);
		entry.comment = comment;
		return this;
	}

	public void close() {
		try {
			fout.close();
		}
		catch (Exception e) {}
		tmp.delete();
	}

	private SHA1 sha(InputStream zin, OutputStream fout) throws NoSuchAlgorithmException, IOException, Exception {
		Digester<SHA1> digester = SHA1.getDigester(fout);
		IO.copy(zin, digester);
		SHA1 digest = digester.digest();
		return digest;
	}
}
