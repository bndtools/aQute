package aQute.poma.db;

import java.math.*;
import java.util.*;

import aQute.poma.domain.*;

public class CustomerImpl implements Customer {
	final String id;
	BigDecimal discount = new BigDecimal("0");
	final ArrayList<BillImpl> bills = new ArrayList<BillImpl>();

	public CustomerImpl(String id) {
		this.id = id;
	}

	@Override
	public String getName() {
		return id;
	}

	@Override
	public Collection<? extends Bill> getBills() {
		return bills;
	}

	@Override
	public Bill createBill() {
		BillImpl e = new BillImpl(this, id, null);
		bills.add(e);
		return e;
	}

	@Override
	public BigDecimal getDiscount() {
		return discount;
	}

	@Override
	public void setDiscount(BigDecimal discount) {
		this.discount = discount;
	}
	
	public String toString() {
		return id;
	}
}
