package aQute.impl.cafs;

import java.io.*;

import aQute.service.cafs.*;
import aQute.service.cafs.data.*;

public class CatalogEntryImpl implements CatalogEntry {
	final CatalogImpl	parent;
	final CAFSImpl		cafs;
	final EntryData		ed;
	CatalogImpl			child;

	public CatalogEntryImpl(CAFSImpl cafs, CatalogImpl parent, EntryData ed) {
		this.cafs = cafs;
		this.parent = parent;
		this.ed = ed;
	}

	public Catalog getParent() {
		return parent;
	}

	public boolean isCatalog() {
		return ed.isCatalog();
	}

	public Catalog getCatalog() throws Exception {
		if (!isCatalog())
			throw new UnsupportedOperationException("Not a catalog");

		if (child == null)
			child = cafs.getCatalog(ed.digest);

		return child;
	}

	public InputStream getContent() throws Exception {
		return cafs.retrieve(ed.digest);
	}

	public String getName() {
		return ed.name;
	}

	public String getComment() {
		return ed.comment;
	}

	public int getSize() {
		return ed.size;
	}

	public long getTime() {
		return ed.time;
	}

	public byte[] getDigest() {
		return ed.digest;
	}

}
