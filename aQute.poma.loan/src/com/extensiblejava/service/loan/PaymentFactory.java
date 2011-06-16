package com.extensiblejava.service.loan;
import java.math.*;

import com.extensiblejava.service.loan.*;
public interface PaymentFactory {
	public Loan createLoan(PaymentSchedule paymentSchedule, BigDecimal cumulativeInterest, BigDecimal cumulativePrincipal);
	public PaymentSchedule createPaymentSchedule();
	public Payment createPayment(BigDecimal principal, BigDecimal interest);
}
