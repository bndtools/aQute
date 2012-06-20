package com.extensiblejava.service.loan;

import java.math.*;

public interface LoanCalculator {
	public Loan calculateLoan(BigDecimal presentValue, BigDecimal rate, int term) throws CalculationException;
	/*
	 * public BigDecimal getCumulativeInterest(); public BigDecimal
	 * getCumulativePrincipal();
	 */

}