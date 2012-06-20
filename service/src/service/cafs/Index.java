package service.cafs;

import java.util.*;

public interface Index extends Map<OID,Index.Entry> {
	public enum Type {
		BLOB, INDEX, OTHER
	};

	public interface Entry {

		Type getType();

		String getPath();

		OID getOID();

		long getSize();

		long getTime();

		Map<String,String> getProperties();

		void remove();
	}

	Entry addEntry(Index.Type type, OID oid, String path, long size, long time);

	Map<String,String> getProperties();

	OID store();

	Iterator<Entry> iterate();

	Entry get(OID oid);

	Entry get(String path);

	OID getPrevious();

	long getTime();
}
