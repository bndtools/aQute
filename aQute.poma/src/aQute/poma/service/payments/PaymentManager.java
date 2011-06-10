package aQute.poma.service.payments;

import aQute.poma.domain.*;
import aQute.poma.service.gateway.*;

/**
 * An actor that can handle the payments.
 * 
 */
public interface PaymentManager {
	void pay(Bill bill, Payment payment);
}
