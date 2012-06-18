package service.cafs;

import java.io.*;
import java.util.*;
import java.util.zip.*;

public interface CAFS {
	OID store(InputStream in) throws Exception;

	Index storeAll(ZipInputStream in) throws Exception;

	StoreOutputStream store() throws Exception;

	InputStream retrieve(OID oid) throws Exception;

	ZipInputStream retrieveAll(Collection<OID> oids) throws Exception;

	boolean contains(OID oid) throws Exception;

	Collection<OID> missing(Collection<OID> oids) throws Exception;

	Index createIndex(OID previous);
}
