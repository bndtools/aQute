package aQute.service.library;

import java.net.*;

import aQute.data.libsync.*;
import aQute.impl.store.mongo.*;


public interface Library {
	Item checkin(URL url) throws Exception;
	Item item(byte[] id) throws Exception;
	MongoCursorImpl<ItemData> find() throws Exception;
}
