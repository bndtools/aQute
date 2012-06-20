package com.extensiblejava.service.loan;

import java.math.*;

public interface Payment {
	public BigDecimal getPrincipal();

	public BigDecimal getInterest();
}
