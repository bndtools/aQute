package aQute.data.libsync;

import static aQute.data.common.CommonPatterns.*;
import aQute.data.*;

public class Project {
	final static String	PROJECT	= TEXT + "(+" + TEXT + ")*";

	@Match(PROJECT)
	String				_id;

}
