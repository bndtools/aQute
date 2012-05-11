package aQute.impl.library;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

import org.osgi.service.log.*;

import aQute.bnd.annotation.component.*;
import aQute.data.libsync.*;
import aQute.impl.store.mongo.*;
import aQute.service.cafs.*;
import aQute.service.library.*;

@Component
public class LibraryImpl implements Library {
	final List<LibraryImporter>	importers	= new CopyOnWriteArrayList<LibraryImporter>();
	CAFS						cafs;
	LogService					log;
	MongoStoreImpl<ItemData>				items;

	public LibraryImpl() {
		importers.add(new ZipImporter());
	}

	public Item checkin(URL location) throws Exception {
		ItemData item = new ItemData();
		item.location = location;
		URLConnection connection = location.openConnection();
		connection.connect();

		item.mime = connection.getContentType();
		if ( item.mime == null)
			item.mime = "application/octet";
		else
			item.mime = item.mime.toLowerCase();
		
		item.reportedMime = item.mime;
		
		int n = location.getPath().lastIndexOf('.');
		if (n > 0) item.extension = location.getPath().substring(n+1).toLowerCase();

		for (LibraryImporter importer : importers) {
			if (importer.canHandle(item)) {
				try {
					InputStream in = connection.getInputStream();
					try {
						importer.checkin(in, cafs, item);
						item.imported = new Date();
						
						item._id = items.uniqueId();
						return new ItemImpl(this, item);
					} finally {
						in.close();
					}
				} catch (Exception e) {
					e.printStackTrace();
					log.log(LogService.LOG_ERROR, "Handler fails to import", e);
					// we screwed up the input stream ...
					connection = location.openConnection();
				}
			}
		}
		return null;
	}

	public Item item(byte[] id) throws Exception {
		ItemData select = new ItemData();
		select._id = id;
		ItemData first = items.find(select).first();
		if ( first == null)
			return null;
		
		return new ItemImpl(this,first);
	}

	public MongoCursorImpl<ItemData> find() throws Exception {
		return items.all();
	}
	
	
	
	@Reference
	void setCAFS(CAFS cafs) {
		this.cafs = cafs;
	}

	
	@Reference
	void setLog(LogService log) {
		this.log = log;
	}

	@Reference(type = '*')
	void addImporter(LibraryImporter importer) {
		importers.add(importer);
	}

	void removeImporter(LibraryImporter importer) {
		importers.remove(importer);
	}
}
