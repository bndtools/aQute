package aQute.aws.sdb;

import java.util.*;

import aQute.aws.sdb.Domain.*;


public class SelectRequestImpl<T> implements SelectRequest<T> {
	int				limit			= -1;
	boolean			consistentRead	= false;
	boolean			ascending		= true;
	String			orderBy;
	SDBImpl			parent;
	DomainImpl<T>	domain;
	String			where;

	public SelectRequestImpl(SDBImpl parent, DomainImpl<T> domain,
			String where, Object... args) {
		this.parent = parent;
		this.domain = domain;
		this.where = format(where, args);
	}

	@Override
	public Iterator<T> iterator() {
		return select().iterator();
	}

	@Override
	public SelectRequest<T> consistentRead() {
		this.consistentRead = true;
		return this;
	}

	@Override
	public SelectRequest<T> descending() {
		ascending = false;
		return this;
	}

	@Override
	public SelectRequest<T> orderBy(String orderBy) {
		this.orderBy = orderBy;
		return this;
	}

	@Override
	public SelectRequest<T> orderByItemName() {
		this.orderBy = "itemName()";
		return this;
	}

	@Override
	public int count() {
		return 0;
	}

	@Override
	public Collection<T> select(String... attrs) {
		StringBuilder sb = new StringBuilder("select");
		String del = " ";
		for (String attr : attrs) {
			sb.append(del);
			quote(sb, '`', attr);
		}
		if (orderBy != null) {
			sb.append(" order by ");
			quote(sb, '`', orderBy);
		}
		if (!ascending)
			sb.append(" desc");

		return null;
	}

	@Override
	public SelectRequest<T> limit(int limit) {
		this.limit = limit;
		return this;
	}

	@Override
	public Collection< ? > itemNames() {
//		Collection< ? > os = select("itemName()");
		return null;
	}

	private String format(String where, Object... args) {
		StringBuilder sb = new StringBuilder();
		int n = 0;
		
		for (int i = 0; i < where.length(); i++) {
			char c = where.charAt(i);
			if (c == '%') {
				c = where.charAt(i++);
				switch (c) {
					case '%' :
						sb.append('%');
						break;
						
					case 's' :
						
					default :
						throw new IllegalArgumentException(where);
				}
			}
		}
		return sb.toString();
	}

	private void quote(StringBuilder sb, char c, String attr) {
		// TODO Auto-generated method stub

	}

}
