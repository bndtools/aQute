package com.extensiblejava.impl.dumbclient;

import java.math.*;

import com.extensiblejava.service.facade.*;

import aQute.bnd.annotation.component.*;

@Component
public class DumbClientImpl {
	LoanFacade loanFacade;

	@Activate
	void activate() {
		BigDecimal payment = this.loanFacade.getMonthlyPayment(new BigDecimal(
				"15000"), new BigDecimal("12"), 60);
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
