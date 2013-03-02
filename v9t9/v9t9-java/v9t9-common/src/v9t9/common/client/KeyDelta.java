/*
  KeyDelta.java

  (c) 2012 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
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