package aQute.poma.service.db;

import aQute.poma.domain.*;

/**
 * Represents the database with customers.
 */
public interface DB {
	Customer getCustomer(String id);
}
