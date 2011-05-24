package aQute.poma.service.payments;

import aQute.poma.domain.*;

/**
 * An actor that can handle the payments.
 * 
 */
public interface PaymentManager {
	void pay(Bill bill, Payment payment);
}
