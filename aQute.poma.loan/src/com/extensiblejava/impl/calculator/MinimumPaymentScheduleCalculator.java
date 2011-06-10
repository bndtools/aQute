package com.extensiblejava.impl.calculator;

import java.math.*;

import com.extensiblejava.service.loan.*;

import aQute.bnd.annotation.component.*;

@Component
public class MinimumPaymentScheduleCalculator implements LoanCalculator {
	private PaymentFactory paymentFactory;

	public Loan calculateLoan(BigDecimal presentValue, BigDecimal rate, int term)
			throws CalculationException {
		System.out.println("---** IN JAVA CALCULATOR **---");
		BigDecimal cumulativePrincipal = new BigDecimal("0");
		BigDecimal cumulativeInterest = new BigDecimal("0");
		try {
			PaymentSchedule paymentSchedule = this.paymentFactory
					.createPaymentSchedule();
			BigDecimal adjustedRate = rate.divide(new BigDecimal("1200"), 2,
					BigDecimal.ROUND_HALF_UP);
			MonthlyPaymentCalculator paymentCalculator = new MonthlyPaymentCalculator();
			BigDecimal monthlyPayment = paymentCalculator.calculatePayment(
					presentValue, rate, term);
			BigDecimal loanBalance = new BigDecimal(presentValue.toString());
			while (loanBalance.doubleValue() > monthlyPayment.doubleValue()) {
				BigDecimal interest = loanBalance.multiply(adjustedRate);
				interest = interest.setScale(2, BigDecimal.ROUND_HALF_UP);
				BigDecimal principal = monthlyPayment.subtract(interest);
				principal = principal.setScale(2, BigDecimal.ROUND_HALF_UP);
				Payment payment = this.paymentFactory.createPayment(principal,
						interest);
				paymentSchedule.addPayment(payment);

				cumulativeInterest = cumulativeInterest.add(interest).setScale(
						2, BigDecimal.ROUND_HALF_UP);
				cumulativePrincipal = cumulativePrincipal.add(principal)
						.setScale(2, BigDecimal.ROUND_HALF_UP);
				loanBalance = loanBalance.subtract(principal);
			}

			BigDecimal interest = loanBalance.multiply(adjustedRate).setScale(
					2, BigDecimal.ROUND_HALF_UP);
			BigDecimal principal = loanBalance.setScale(2,
					BigDecimal.ROUND_HALF_UP);
			cumulativeInterest = cumulativeInterest.add(interest).setScale(2,
					BigDecimal.ROUND_HALF_UP);
			cumulativePrincipal = cumulativePrincipal.add(principal).setScale(
					2, BigDecimal.ROUND_HALF_UP);
			Payment payment = this.paymentFactory.createPayment(principal,
					interest);
			paymentSchedule.addPayment(payment);
			return this.paymentFactory.createLoan(paymentSchedule,
					cumulativeInterest, cumulativePrincipal);
		} catch (Exception e) {
			throw new CalculationException(e);
		}
	}

	@Reference
	void setPaymentFactory(PaymentFactory paymentFactory) {
		this.paymentFactory = paymentFactory;
	}
}