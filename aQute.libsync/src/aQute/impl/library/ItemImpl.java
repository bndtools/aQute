package aQute.impl.library;

import java.net.*;
import java.util.*;

import aQute.data.libsync.*;
import aQute.service.cafs.*;
import aQute.service.library.*;

public class ItemImpl implements Item {
	final LibraryImpl lib;
	final ItemData item;
	
	public ItemImpl(LibraryImpl lib, ItemData item) {
		this.lib = lib;
		this.item = item;
	}

	public URL getLocation() {
		return item.location;
	}

	public byte[] getId() {
		return item._id;
	}

	public Catalog getCatalog() throws Exception {
		return lib.cafs.getCatalog(item.caf);
	}

	public Date getImported() {
		return item.imported;
	}

	public String getMime() {
		return item.mime;
	}

	public URL getCached() {
		// TODO Auto-generated method stub
		return null;
	}

}
