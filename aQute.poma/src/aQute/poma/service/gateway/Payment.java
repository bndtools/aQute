package aQute.poma.service.gateway;

import java.math.*;

import aQute.poma.domain.*;


/**
 * Represents a parameterized payment. A Payment is made by a
 * Payment Gateway who knows exactly what kind of data is needed
 * for a particular payment method. The Payment is completely
 * prepared, the only thing need is to call transfer on it
 * to activate the payment.
 */
public interface Payment {
	void transfer(BigDecimal amount);
}
