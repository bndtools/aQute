package com.extensiblejava.impl.loan;
import java.math.*;

import com.extensiblejava.service.loan.*;


public class PaymentImpl implements Payment {
	private BigDecimal principal;
	private BigDecimal interest;
	public PaymentImpl(BigDecimal principal, BigDecimal interest) {
		this.principal = principal;
		this.interest = interest;
	}

	public BigDecimal getPrincipal() { return this.principal; }
	public BigDecimal getInterest() { return this.interest; }
}
