package service.keyvaluedb;

import java.util.*;

public interface Domain<T> {

	T get(Object id);

	Iterator<T> get(Object... id);

	void update(T... items);

	Iterator<T> select(String where, Object... args) throws Exception;

	int count(String select, Object... args) throws Exception;

	Updater<T> updater();

}
