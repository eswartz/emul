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
	public int key;
	public byte shift;
	public boolean synthetic;
	public boolean onoff;
	public boolean isShift;
	public long time;
	public int realKey;
	public KeyDelta(long time, int realKey, int key, byte shift, boolean synthetic, boolean onoff) {
		this.time = time;
		this.realKey = realKey;
		this.key = key;
		this.shift = shift;
		this.synthetic = synthetic;
		this.onoff = onoff;
		this.isShift = (shift != 0) && (key == 0);
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