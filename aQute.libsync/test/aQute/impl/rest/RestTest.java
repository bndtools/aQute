package aQute.impl.rest;

import java.util.*;

import junit.framework.*;
import aQute.service.rest.*;

public class RestTest extends TestCase {

	public class Card {
		String	_id;
		String	nameOnCard;
	}

	public class User {
		public byte[]		_id;
		public String		name;
		Collection<Card>	cards;

	}

	public class UserManaer implements ResourceManager {

		// // PUT /rest/user
		// public String putUser( User user) {return null;}
		//
		// // PUT /rest/user/123
		// public void putUser( User user, String id) {}
		//
		// public void postCard( options options, String userId) {
		//
		// }
		// public User get(options options, String id) {
		//
		// }
		//
		// public Card getCard(options options, String userId, String cardId) {
		//
		// }
		//
		// public Iterable<Card> getCard(options options, String userId) {
		//
		// }
		//
		//
		// public Card getCard(options options) {
		//
		// }
		//
		// public Iterable<User> get(query query) {
		//
		// }
		//
		// public void delete(String id) {
		//
		// }
		//
		// public void deleteCard(String userId, String cardId) {
		//
		// }
		//
	}
}
