package service.keyvaluedb;

import java.util.*;
import java.util.concurrent.*;

public interface Updater<T> {
	void delete(T item, String... attrs);

	void replace(T item, String... attrs);

	void set(T item, String... attrs);

	Future<Collection<T>> batch();
}
