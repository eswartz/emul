/*
  FullMemoryWriteTracker.java

  (c) 2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
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
	protected void recordChange(int addr, int size, int value) {
		synchronized (changes) {
			if (size == 1) {
				changes.add((addr << 24) | (value & 0xff));
//			} else if (value == null) {
//				changes.add((addr << 24) | (domain.flatReadByte(addr) & 0xff));
			} else if (size == 2) {
				changes.add((addr << 24) | ((value >> 8) & 0xff));
				changes.add(((addr + 1) << 24) | (value & 0xff));
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
