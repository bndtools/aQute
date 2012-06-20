package aQute.poma.service.audit;

import java.math.*;

import aQute.poma.domain.*;

/**
 * An auditor is called during the payment process and can modify the amount.
 */
public interface Auditor {

	/**
	 * Called during the payment process.
	 * 
	 * @param auditable
	 * @return new amount
	 */
	BigDecimal audit(Bill auditable);
}
