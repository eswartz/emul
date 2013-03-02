/*
  ListenerList.java

  (c) 2011-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package ejs.base.utils;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * A thread-safe list of listeners.
 * @author ejs
 *
 */
public class ListenerList<T> implements Iterable<T>, Serializable {

	private static final long serialVersionUID = -4548014975540398630L;

	public interface IFire<T> {
		void fire(T listener);
	}
	
	private static final Object[] NO_LISTENERS = new Object[0];
	private Set<T> listeners;
	private transient Object[] listenerArray;
	
	public ListenerList() {
		listeners = new HashSet<T>(1);
		listenerArray = NO_LISTENERS;
	}
	
	/**
	 * Add a listener, ignoring duplicates
	 * @param listener
	 */
	public void add(T listener) {
		if (listener == null)
			throw new NullPointerException();
		synchronized (listeners) {
			if (!listeners.contains(listener)) {
				listeners.add(listener);
				listenerArray = listeners.toArray(new Object[listeners.size()]);
			}
		}
	}
	
	/**
	 * Remove a listener, ignoring missing entries
	 * @param listener
	 */
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
		Object[] localArray = listenerArray;
		for (Object obj : localArray) {
			@SuppressWarnings("unchecked")
			T listener = (T) obj;
			try {
				fire.fire(listener);
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
	}

	@Override
	public Iterator<T> iterator() {
		synchronized (listeners) {
			return new Iterator<T>() {
	
				Object[] localArray = listenerArray;
				int idx = 0;
				
				@Override
				public boolean hasNext() {
					return idx < localArray.length;
				}
	
				@SuppressWarnings("unchecked")
				@Override
				public T next() {
					if (idx >= localArray.length)
						throw new NoSuchElementException();
					return (T) localArray[idx++];
				}
	
				@SuppressWarnings("unchecked")
				@Override
				public void remove() {
					if (idx == 0)
						throw new IllegalStateException();
					ListenerList.this.remove((T) localArray[idx - 1]);
				}
			};
		}
	}
	
	public void clear() {
		synchronized (listeners) {
			listeners.clear();
			listenerArray = NO_LISTENERS;
		}
	}

	/**
	 * @return
	 */
	public boolean isEmpty() {
		return listenerArray == NO_LISTENERS;
	}

	/**
	 * Get the array of listeners (faster than iterating or invoking #fire(), but you must 
	 * iterate and handle exceptions from each callback yourself)
	 * @return
	 */
	public Object[] toArray() {
		return listenerArray;
	}
}
