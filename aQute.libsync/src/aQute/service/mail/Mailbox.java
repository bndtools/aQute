package aQute.service.mail;

public interface Mailbox {
	/**
	 * An address consists a contiguous combination of letters, digits, and/or
	 * '-' or '_'
	 */
	String	ADDRESS	= "address";

	void deliver(MessageData md);
}
