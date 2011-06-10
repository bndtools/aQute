package com.extensiblejava.impl.loan;
import java.math.*;

import com.extensiblejava.service.loan.*;

import aQute.bnd.annotation.component.*;

@Component
public class PaymentFactoryImpl implements PaymentFactory {
	public Loan createLoan(PaymentSchedule paymentSchedule, BigDecimal cumulativeInterest, BigDecimal cumulativePrincipal) { 
		return new LoanImpl(paymentSchedule, cumulativeInterest, cumulativePrincipal); 
	}
	public PaymentSchedule createPaymentSchedule() { return new PaymentScheduleImpl(); }
	public Payment createPayment(BigDecimal principal, BigDecimal interest) { return new PaymentImpl(principal, interest); }
}
