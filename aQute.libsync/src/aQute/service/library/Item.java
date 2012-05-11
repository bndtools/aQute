package aQute.service.library;

import java.net.*;
import java.util.*;

public interface Item {
	URL getLocation();
	byte[] getId();
	Date getImported();
	String getMime();
	URL getCached();
}
