package aQute.poma.simpleauditor;

import java.math.*;

import aQute.bnd.annotation.component.*;
import aQute.poma.domain.*;
import aQute.poma.service.audit.*;

/**
 * An implementation of a trivial auditor.
 * 
 */
@Component
public class SimpleAuditor implements Auditor {

	@Override
	public BigDecimal audit(Bill bill) {
		System.out.println("Simple auditing " + bill.getId());
		BigDecimal amount = bill.getAmount();
		BigDecimal discount = bill.getCustomer().getDiscount();
		BigDecimal t = amount.multiply(discount);
		return amount.subtract(t);
	}

}
