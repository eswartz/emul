package v9t9.common.memory;

import java.util.BitSet;


/**
 * Track a series of changes to a memory domain
 * @author ejs
 *
 */
public class MemoryWriteTracker {
	final IMemoryDomain domain;
	BitSet changedMemory = new BitSet();
	private IMemoryWriteListener memoryWriteListener;
	private final int addr;
	private final int size;
	
	public MemoryWriteTracker(IMemoryDomain domain, int addr, int size) {
		this.domain = domain;
		this.addr = addr;
		this.size = size;
	}
	public void addMemoryListener() {
		if (memoryWriteListener == null) {
			memoryWriteListener = new IMemoryWriteListener() {
	
				public void changed(IMemoryEntry entry, int addr, final Number value) {
					synchronized (MemoryWriteTracker.this) {
						synchronized (changedMemory) {
							if (addr >= MemoryWriteTracker.this.addr 
									&& addr < MemoryWriteTracker.this.addr + size) {
								changedMemory.set(addr);
								if (value instanceof Short) {
									changedMemory.set(addr + 1);
								}
							}
						}
					}
				}
				
			};
			
			synchronized (changedMemory) {
				changedMemory.clear();
			}
			domain.addWriteListener(memoryWriteListener);
		}
	}
	
	public BitSet getChangedMemory() {
		return changedMemory;
	}
	
	public void removeMemoryListener() {
		if (memoryWriteListener != null) {
			domain.removeWriteListener(memoryWriteListener);
			memoryWriteListener = null;
		}
	}
}