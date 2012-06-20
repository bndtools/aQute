package aQute.poma.domain;

import java.math.*;

public interface Bill {
	enum Payment {
		UNPAID, TRANSFER, SETTLED
	};

	String getId();

	BigDecimal getAmount();

	Customer getCustomer();

	void setPaid(Payment b);

	Payment getPaid();
}
