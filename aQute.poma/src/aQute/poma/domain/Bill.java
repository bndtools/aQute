package aQute.poma.domain;

import java.math.*;

public interface Bill {
	String getId();
	BigDecimal getAmount();
	Customer getCustomer();
	void setPaid(boolean b);
}
