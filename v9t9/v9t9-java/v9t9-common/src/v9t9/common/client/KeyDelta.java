package v9t9.common.client;

import java.util.List;

public class KeyDelta {
	public static final boolean DEBUG = false;
	@Override
	public String toString() {
		return "KeyDelta [key=" + (key > 0 ? (char) key : "---") + ", onoff=" + onoff + ", shift="
				+ shift + ", isShift="+isShift+", synthetic=" + synthetic + ", time=" + time
				+ "]";
	}
	final public int key;
	final public byte shift;
	final public boolean synthetic;
	final public boolean onoff;
	final public boolean isShift;
	final public long time;
	final public int realKey;
	public KeyDelta(long time, int realKey, int key, byte shift, boolean synthetic, boolean onoff) {
		this.time = time;
		this.realKey = realKey;
		this.key = key;
		this.shift = shift;
		this.synthetic = synthetic;
		this.onoff = onoff;
		this.isShift = (shift != 0) && (key == 0);
	}
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (isShift ? 1231 : 1237);
		result = prime * result + key;
		result = prime * result + (onoff ? 1231 : 1237);
		result = prime * result + realKey;
		result = prime * result + shift;
		result = prime * result + (synthetic ? 1231 : 1237);
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
		if (isShift != other.isShift)
			return false;
		if (key != other.key)
			return false;
		if (onoff != other.onoff)
			return false;
		if (realKey != other.realKey)
			return false;
		if (shift != other.shift)
			return false;
		if (synthetic != other.synthetic)
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
			} else if (false && oldestTime + 100 < time){
				return false;
			} 
			if (delta.isShift) {
				continue;
			}
			else if (delta.key != key && delta.onoff && onoff) {
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
	
}