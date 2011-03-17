package aQute.metatype.samples;

import java.net.*;
import java.util.*;

import aQute.bnd.annotation.metatype.*;


@Metadata.OCD
public interface SampleConfig {
	enum X { A, B, C; }
	String name();
	int birthYear();
	URI uri();
	URI[] uris();
	Collection<URI> curls();
	int port();
	X x();
	int[] ints();
	//Collection<Integer> ints(); does not work in the webconsole
}
