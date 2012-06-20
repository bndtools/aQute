package aQute.service.speak;

/**
 * A service that can leverage speech synthesizers.
 */
public interface Speak {
	/**
	 * Say the message through a speech synthesizer.
	 * 
	 * @param message
	 *            The message to say
	 * @throws Exception
	 *             If anything fails in the process
	 */
	void say(String message) throws Exception;
}
