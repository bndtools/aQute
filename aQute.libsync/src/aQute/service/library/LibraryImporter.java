package aQute.service.library;

import java.io.*;

import aQute.data.libsync.*;
import aQute.service.cafs.*;

public interface LibraryImporter {
	void checkin(InputStream inputStream, CAFS cafs, ItemData item) throws Exception;


	boolean canHandle(ItemData item);
}
