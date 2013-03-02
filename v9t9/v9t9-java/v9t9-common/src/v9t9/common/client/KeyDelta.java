/*
  KeyDelta.java

  (c) 2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.client;

import java.util.List;

import v9t9.common.keyboard.KeyboardConstants;

public class KeyDelta {
	public static final boolean DEBUG = false;
	@Override
	public String toString() {
		return "KeyDelta [key=" + (key >= 32 ? (char) key : "#"+String.valueOf(key)) + ", onoff=" + onoff + ", time=" + time
				+ "]";
	}
	final public int key;
	final public boolean onoff;
	final public long time;
	public KeyDelta(boolean onoff, int key) {
		this.time = System.currentTimeMillis();
		this.key = key;
		this.onoff = onoff;
		//this.isShift = (shift != 0) && (key == 0);
	}
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + key;
		result = prime * result + (onoff ? 1231 : 1237);
		result = prime * result + (int) (time ^ (time >>> 32));
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		KeyDelta other = (KeyDelta) obj;
		if (key != other.key)
			return false;
		if (onoff != other.onoff)
			return false;
		if (time != other.time)
			return false;
		return true;
	}


	/**
	 * Tell if this delta is part of a combination with the
	 * current list of deltas (and is not a distinct keystroke)
	 * @param currentGroup
	 * @return
	 */
	public boolean groupsWith(List<KeyDelta> currentGroup) {
		long oldestTime = 0;
		for (KeyDelta delta : currentGroup) {
			if (oldestTime == 0) {
				oldestTime = delta.time;
			} 
			if (delta.isShift()) {
				continue;
			}
			else 
			if (delta.key != key && delta.onoff && onoff) {
				return false;
			}
			else if (delta.key == key && (delta.onoff != onoff)) {
				if (!onoff)
					return false;
			}
			else if (delta.key == key && oldestTime + (1000 / 30) <= time && onoff) {
				return false;
			}
			if (DEBUG) System.out.println("... keeping " + delta + " with " + this);
		}
		return true;
	}


	/**
	 * @return
	 */
	public boolean isShift() {
		return key >= KeyboardConstants.KEY_SHIFT && key <= KeyboardConstants.KEY_CONTEXT;
	}
	
}