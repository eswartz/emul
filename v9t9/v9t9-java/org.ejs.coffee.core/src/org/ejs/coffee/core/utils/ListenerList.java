/**
 * Jul 24, 2011
 */
package org.ejs.coffee.core.utils;

import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * A thread-safe list of listeners.
 * @author ejs
 *
 */
public class ListenerList<T> implements Iterable<T> {

	public interface IFire<T> {
		void fire(T listener);
		void threw(T listener, Throwable t);
	}
	
	private static final Object[] NO_LISTENERS = new Object[0];
	private Set<T> listeners;
	private transient Object[] listenerArray;
	
	public ListenerList() {
		listeners = new HashSet<T>(1);
		listenerArray = NO_LISTENERS;
	}
	
	public void add(T listener) {
		if (listener == null)
			throw new NullPointerException();
		synchronized (listeners) {
			listeners.add(listener);
			listenerArray = listeners.toArray(new Object[listeners.size()]);
		}
	}
	
	public void remove(T listener) {
		synchronized (listeners) {
			if (listeners.remove(listener)) {
				if (listeners.isEmpty()) {
					listenerArray = NO_LISTENERS;
				} else {
					listenerArray = listeners.toArray(new Object[listeners.size()]);
				}
			}
		}
	}
	
	public void fire(IFire<T> fire) {
		Object[] copy = listenerArray;
		for (Object obj : copy) {
			@SuppressWarnings("unchecked")
			T listener = (T) obj;
			try {
				fire.fire(listener);
			} catch (Throwable t) {
				fire.threw(listener, t);
			}
		}
	}

	@Override
	public Iterator<T> iterator() {
		return new Iterator<T>() {

			int idx = 0;
			
			@Override
			public boolean hasNext() {
				return idx < listenerArray.length;
			}

			@SuppressWarnings("unchecked")
			@Override
			public T next() {
				if (idx >= listenerArray.length)
					throw new NoSuchElementException();
				return (T) listenerArray[idx++];
			}

			@SuppressWarnings("unchecked")
			@Override
			public void remove() {
				if (idx == 0)
					throw new IllegalStateException();
				ListenerList.this.remove((T) listenerArray[idx - 1]);
			}
		};
	}
	
	public void clear() {
		synchronized (listeners) {
			listeners.clear();
			listenerArray = NO_LISTENERS;
		}
	}
}
