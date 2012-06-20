package aQute.poma.domain;

import java.math.*;
import java.util.*;

public interface Customer {
	String getName();

	Collection< ? extends Bill> getBills();

	Bill createBill();

	BigDecimal getDiscount();

	void setDiscount(BigDecimal discount);
}
