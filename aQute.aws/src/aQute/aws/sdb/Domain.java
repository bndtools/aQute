package aQute.aws.sdb;

import java.util.*;
import java.util.concurrent.atomic.*;

public interface Domain<T> {
	public class Expected<V> extends AtomicReference<V> {
		private static final long	serialVersionUID	= 1L;

		V							expectedValue;

		public Expected(V value) {
			super(value);
		}

		public V getExpected() {
			return expectedValue;
		}

		public void setExpected(V value) {
			expectedValue = value;
		}
	}

	public class IfExists<V> extends AtomicReference<V> {
		private static final long	serialVersionUID	= 1L;

		public IfExists(V value) {
			super(value);
		}

	}

	public class IfNotExists<V> extends AtomicReference<V> {
		private static final long	serialVersionUID	= 1L;

		public IfNotExists(V value) {
			super(value);
		}

	}

	public interface SelectRequest<T> extends Iterable<T> {
		int count();
		Collection<?> itemNames();
		Collection<T> select(String... attrs);
		
		SelectRequest<T> consistentRead();
		SelectRequest<T> descending();
		SelectRequest<T> orderBy(String attribute);
		SelectRequest<T> limit(int limit);
		SelectRequest<T> orderByItemName();
		
	}

	public interface Op<T> {
		WhereRequest<T> notEquals(Object value);
		WhereRequest<T> equal(Object value);
		WhereRequest<T> like(Object value);
		WhereRequest<T> notLike(Object value);
		WhereRequest<T> greater(Object value);
		WhereRequest<T> greaterOrEqual(Object value);
		WhereRequest<T> less(Object value);
		WhereRequest<T> lessOrEqual(Object value);
		WhereRequest<T> in(Object ... value);
		WhereRequest<T> isNull();
		WhereRequest<T> isNotNull();
		WhereRequest<T> every();
		WhereRequest<T> between(Object low, Object high);
	}
	public interface WhereRequest<T> extends SelectRequest<T> {
		WhereRequest<T> intersection( WhereRequest<T> rq );
		WhereRequest<T> and( WhereRequest<T> rq );
		WhereRequest<T> or( WhereRequest<T> rq );
		WhereRequest<T> not( WhereRequest<T> rq );
		Op<T> itemName();
		
	}
	public interface ConditionalRequest<T> {
		ConditionalRequest<T> and(T item);

		ConditionalRequest<T> exepectedValue(String field, Object value);

		ConditionalRequest<T> ifExists(String field);

		ConditionalRequest<T> ifNotExists(String field);

		void delete();

		void put();
	}

	public interface MultiGetRequest<T> {
		MultiGetRequest<T> key(Object id);

		Collection<T> get();

		void delete();
	}

	public interface MultiRequest<T> {
		MultiRequest<T> item(T item);

		void put() throws Exception;

		void delete() throws Exception;
	}

	T get(Object id) throws Exception;

	MultiRequest<T> item(T item);

	MultiGetRequest<T> id(Object key);

	ConditionalRequest<T> conditional(T item) throws Exception;

	Op<T> where(String key) throws Exception;

}
