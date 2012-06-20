package aQute.data;

import java.util.*;

public class Id {
	public byte[]	_id;

	public boolean equals(Object other) {
		return (other instanceof Id) && Arrays.equals(_id, ((Id) other)._id);
	}

	public int hashCode() {
		return Arrays.hashCode(_id);
	}
}
