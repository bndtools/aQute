package aQute.data.common;

import java.util.*;

public class PersonStruct {
	public String				_id;
	public String				firstName;
	public String				initial;
	public String				lastName;
	public List<EmailStruct>	email	= new ArrayList<EmailStruct>();
}
