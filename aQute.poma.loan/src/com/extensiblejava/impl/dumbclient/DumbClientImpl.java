package com.extensiblejava.impl.dumbclient;

import java.math.*;

import aQute.bnd.annotation.component.*;

import com.extensiblejava.service.facade.*;

@Component
public class DumbClientImpl {
	LoanFacade	loanFacade;

	@Activate
	void activate() {
		BigDecimal payment = this.loanFacade.getMonthlyPayment(new BigDecimal("15000"), new BigDecimal("12"), 60);
		System.out.println("Payment: " + payment);

	}

	@Deactivate
	void stop() throws Exception {
		System.out.println("GOODBYE LOAN!");
	}

	@Reference
	void setLoanFacade(LoanFacade loanFacade) {
		this.loanFacade = loanFacade;
	}
}
