package aQute.poma.simplepayment;

import java.math.*;

import aQute.bnd.annotation.component.*;
import aQute.poma.domain.*;
import aQute.poma.service.audit.*;
import aQute.poma.service.gateway.*;
import aQute.poma.service.payments.*;

/**
 * An implementation of a trivial payemtn manager.
 */
@Component
public class SimplePayments implements PaymentManager {

	private Auditor auditor;


	public void pay(Bill bill, Payment option) {
		BigDecimal discount = auditor.audit(bill);
		BigDecimal amount = bill.getAmount();
		amount.subtract(discount);
		option.transfer( amount);
		
		bill.setPaid(Bill.Payment.TRANSFER);
	}
	
	
	@Reference 
	void setAudit( Auditor auditor ) {
		this.auditor = auditor;
	}
}
