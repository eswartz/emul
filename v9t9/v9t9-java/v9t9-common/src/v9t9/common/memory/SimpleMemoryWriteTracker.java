/*
  SimpleMemoryWriteTracker.java

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

import java.util.BitSet;


/**
 * Track a series of changes to a memory domain
 * @author ejs
 *
 */
public class SimpleMemoryWriteTracker extends BaseMemoryWriteTracker {
	private BitSet changedMemory = new BitSet();
	
	public SimpleMemoryWriteTracker(IMemoryDomain domain, int granularityShift) {
		super(domain, granularityShift);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.memory.BaseMemoryWriteTracker#recordChange(int, byte)
	 */
	@Override
	protected void recordChange(int addr, Number value) {
		synchronized (changedMemory) {
			changedMemory.set(addr);
		}
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.memory.BaseMemoryWriteTracker#clearChanges()
	 */
	@Override
	public void clearChanges() {
		synchronized (changedMemory) {
			changedMemory.clear();
		}
	}
	
	public BitSet getChangedMemory() {
		return changedMemory;
	}
}