package aQute.impl.cafs;

import java.util.*;

import aQute.service.cafs.*;
import aQute.service.cafs.data.*;

public class CatalogImpl implements Catalog {
	final CatalogData			cd;
	final CAFSImpl				cafs;
	Map<String,CatalogEntry>	cache;

	public CatalogImpl(CAFSImpl cafs, CatalogData cd) {
		this.cafs = cafs;
		this.cd = cd;
	}

	public CatalogEntry getEntry(String path) throws Exception {
		int n = path.indexOf('/');
		if (n > 0) {
			String remainder = path.substring(n + 1);
			String part = path.substring(0, n + 1);
			CatalogEntry entry = getCache().get(part);
			if (entry == null)
				return null;
			if (!entry.isCatalog())
				return null; // stops here

			Catalog catalog = entry.getCatalog();
			return catalog.getEntry(remainder);
		} else {
			return getCache().get(path);
		}
	}

	public Map<String,CatalogEntry> getEntries() {
		// TODO Auto-generated method stub
		return null;
	}

	public Iterator<CatalogEntry> iterator() {
		return getCache().values().iterator();
	}

	private Map<String,CatalogEntry> getCache() {
		if (cache == null) {
			Map<String,CatalogEntry> map = new TreeMap<String,CatalogEntry>();
			if (cd.entries != null)
				for (EntryData e : cd.entries) {
					map.put(e.name, new CatalogEntryImpl(cafs, this, e));
				}
			cache = Collections.unmodifiableMap(map);
		}
		return cache;
	}

	public byte[] getId() {
		return cd.digest;
	}
}
