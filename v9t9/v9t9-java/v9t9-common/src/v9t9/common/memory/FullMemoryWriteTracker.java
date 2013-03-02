/*
  FullMemoryWriteTracker.java

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
package v9t9.common.memory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ejs
 *
 */
public class FullMemoryWriteTracker extends BaseMemoryWriteTracker {

	private List<Integer> changes = new ArrayList<Integer>();
	
	public FullMemoryWriteTracker(IMemoryDomain domain, int granularityShift) {
		super(domain, granularityShift);
	}

	/* (non-Javadoc)
	 * @see v9t9.common.memory.BaseMemoryWriteTracker#recordChange(int, java.lang.Number)
	 */
	@Override
	protected void recordChange(int addr, Number value) {
		synchronized (changes) {
			if (value instanceof Byte) {
				changes.add((addr << 24) | ((Byte) value & 0xff));
			} else if (value == null) {
				changes.add((addr << 24) | (domain.flatReadByte(addr) & 0xff));
			} else if (value instanceof Short) {
				changes.add((addr << 24) | (((Short) value >> 8) & 0xff));
				changes.add(((addr + 1) << 24) | ((Short) value & 0xff));
			} else {
				throw new AssertionError();
			}
		}
	}

	/* (non-Javadoc)
	 * @see v9t9.common.memory.BaseMemoryWriteTracker#clearChanges()
	 */
	@Override
	public void clearChanges() {
		synchronized (changes) {
			changes.clear();
		}
	}
	
	/**
	 * Get a list of changes.  Client may modify but does not own.
	 * 
	 * @return the changes, as &lt;integer24: addr&gt; | &lt;integer8: value&gt;
	 */
	public List<Integer> getChanges() {
		return changes;
	}

}
