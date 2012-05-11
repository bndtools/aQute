package aQute.impl.library;

import java.io.*;
import java.util.*;
import java.util.zip.*;

import aQute.data.libsync.*;
import aQute.service.cafs.*;
import aQute.service.library.*;

public class ZipImporter implements LibraryImporter {
	public static Set<String> mimes = new HashSet<String>();
	public static Set<String> extensions = new HashSet<String>();
	static {
		mimes.add("application/zip");
		mimes.add("application/java-archive");
		mimes.add("application/vnd.osgi.bundle");
		extensions.add("war");
		extensions.add("jar");
		extensions.add("zip");
	}
	
	public void checkin(InputStream in, CAFS cafs, ItemData item)
			throws Exception {
		ZipInputStream zin = new ZipInputStream(in);
		CatalogBuilder cb = cafs.builder();
		ZipEntry entry;
		while ((entry = zin.getNextEntry()) != null) {
			if (!entry.isDirectory())
				cb.add(entry.getName(), new NoCloseInputStream(zin));
			
			cb.setTime(entry.getName(), entry.getTime());

			if (entry.getComment() != null)
				cb.setComment(entry.getName(), entry.getComment());
		}
		Catalog build = cb.build();
		item.caf = build.getId();
	}

	public boolean canHandle(ItemData item) {		
		return mimes.contains(item.mime.toLowerCase()) || extensions.contains(item.extension);
	}

}
