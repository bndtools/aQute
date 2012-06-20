package aQute.poma.db;

import java.math.*;

import aQute.bnd.annotation.component.*;
import aQute.poma.domain.*;
import aQute.poma.service.db.*;

@Component
public class DBImpl implements DB {

	public Customer getCustomer(final String id) {
		CustomerImpl c = new CustomerImpl(id);
		c.bills.add(new BillImpl(c, "1234", new BigDecimal("1200")));
		c.bills.add(new BillImpl(c, "5676", new BigDecimal("12.45")));
		c.bills.add(new BillImpl(c, "4711", new BigDecimal("5613.12")));
		c.setDiscount(new BigDecimal("0.05"));
		return c;
	}
}
