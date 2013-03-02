/*
  MemoryRangeChanges.java

  (c) 2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.swt.shells.debugger;

import java.util.BitSet;

import v9t9.common.memory.SimpleMemoryWriteTracker;

/**
 * @author ejs
 *
 */
public class MemoryRangeChanges {
	private SimpleMemoryWriteTracker tracker;
	protected int lowRange;
	protected int hiRange;
	private BitSet changes;
	private MemoryRange range;
	
	/**
	 * 
	 */
	public MemoryRangeChanges(MemoryRange range) {
		this.range = range;
		tracker = new SimpleMemoryWriteTracker(range.getEntry().getDomain(), 0);
		tracker.addMemoryRange(range.getAddress(), range.getSize());
	}
	public void attachMemoryListener() {
		tracker.addMemoryListener();
		clearTouchRange();
	}

	public void fetchChanges() {
		synchronized (tracker) {
			changes = (BitSet) tracker.getChangedMemory().clone();
			tracker.clearChanges();
			lowRange = changes.nextSetBit(0);
			if (lowRange < 0)
				lowRange = Integer.MAX_VALUE;
			hiRange = changes.length();
		}
	}
	public boolean getAndResetChanged(Integer addr) {
		if (changes == null)
			return false;
		synchronized (tracker) {
			boolean f = changes.get(addr) || (range.getEntry().isWordAccess() && changes.get(addr^1));
			//changes.clear(addr);
			//if (entry.isWordAccess())
			//	changes.clear(addr+1);
			return f;
		}
	}
	public synchronized int getLowTouchRange() {
		return lowRange;
	}
	public synchronized int getHiTouchRange() {
		return hiRange;
	}
	public synchronized boolean isTouched(int addr, int size) {
		if (lowRange > hiRange) 
			return false;
		if (hiRange < addr)
			return false;
		if (lowRange >= addr + size)
			return false;
		return true;
	}
	public synchronized void clearTouchRange() {
		lowRange = Integer.MAX_VALUE;
		hiRange = 0;
	}
	public void removeMemoryListener() {
		tracker.removeMemoryListener();
	}
	/**
	 * @return
	 */
	public BitSet getChangeSet() {
		return changes;
	}
}
