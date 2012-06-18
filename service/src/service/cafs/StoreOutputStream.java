package service.cafs;

import java.io.*;

public abstract class StoreOutputStream extends OutputStream {

	public abstract OID getOid() throws Exception;
}
