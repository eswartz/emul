/*
  SimpleMemoryWriteTracker.java

  (c) 2012-2015 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
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
	protected void recordChange(int addr, int size, int value) {
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