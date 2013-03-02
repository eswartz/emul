/*
  SimpleRegisterWriteTracker.java

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
package v9t9.common.machine;

import java.util.BitSet;
import java.util.Map;
import java.util.TreeMap;


/**
 * Track a series of changes to a set of registers.
 * @author ejs
 *
 */
public class SimpleRegisterWriteTracker extends BaseRegisterWriteTracker {
	private Map<Integer, Integer> changes = new TreeMap<Integer, Integer>(); 
	
	public SimpleRegisterWriteTracker(IRegisterAccess access, int baseReg, BitSet regbits) {
		super(access, baseReg, regbits);
	}

	public SimpleRegisterWriteTracker(IRegisterAccess access) {
		super(access);
	}

	/* (non-Javadoc)
	 * @see v9t9.common.machine.BaseRegisterWriteTracker#clearChanges()
	 */
	@Override
	public void clearChanges() {
		synchronized (changes) {
			changes.clear();
		}
	}

	/* (non-Javadoc)
	 * @see v9t9.common.machine.BaseRegisterWriteTracker#record(int, int)
	 */
	@Override
	protected void record(int reg, int value) {
		synchronized (changes) {
			changes.put(reg, value);
		}
	}
	
	/**
	 * Get the changes, sorted by register key.  Only the last
	 * change to each register is mentioned.
	 * 
	 * Caller may modify
	 * but cannot own the list.
	 * @return the changes
	 */
	public Map<Integer, Integer> getChanges() {
		return changes;
	}
}