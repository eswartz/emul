/**
 * 
 */
package org.ejs.coffee.core.utils;

/**
 * @author ejs
 *
 */
public class Pair<T, U> implements Comparable<Pair<T, U>>{
	public T first;
	public U second;
	public Pair(T first, U second) {
		this.first = first;
		this.second = second;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return first +" | " + second;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((first == null) ? 0 : first.hashCode());
		result = prime * result + ((second == null) ? 0 : second.hashCode());
		return result;
	}
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Pair<T,U> other = (Pair<T,U>) obj;
		if (first == null) {
			if (other.first != null) {
				return false;
			}
		} else if (!first.equals(other.first)) {
			return false;
		}
		if (second == null) {
			if (other.second != null) {
				return false;
			}
		} else if (!second.equals(other.second)) {
			return false;
		}
		return true;
	}
	
	@SuppressWarnings("unchecked")
	public int compareTo(Pair<T, U> o) {
		int diff = ((Comparable<T>)first).compareTo(o.first);
		if (diff != 0)
			return diff;
		diff = ((Comparable<U>)second).compareTo(o.second);
		return diff;
	}

	
	
}
