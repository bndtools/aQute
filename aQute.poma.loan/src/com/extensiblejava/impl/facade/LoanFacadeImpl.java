package com.extensiblejava.impl.facade;

import java.math.*;

import aQute.bnd.annotation.component.*;

import com.extensiblejava.service.facade.*;
import com.extensiblejava.service.loan.*;

@Component
public class LoanFacadeImpl implements LoanFacade {
	private LoanCalculator	loanCalculator;

	public PaymentSchedule calculatePaymentSchedule(BigDecimal presentValue, BigDecimal rate, int term) {
		Loan loan = this.loanCalculator.calculateLoan(presentValue, rate, term);
		PaymentSchedule paymentSchedule = loan.calculatePaymentSchedule();
		return paymentSchedule;
	}

	public BigDecimal getMonthlyPayment(BigDecimal presentValue, BigDecimal rate, int term) {
		Loan loan = this.loanCalculator.calculateLoan(presentValue, rate, term);
		BigDecimal monthlyPayment = loan.getMonthlyPayment();
		return monthlyPayment;
	}

	@Reference
	void setCalc(LoanCalculator l) {
		this.loanCalculator = l;
	}
}