package v9t9.server.tcf;

import java.util.BitSet;

import v9t9.common.memory.IMemoryDomain;
import v9t9.common.memory.IMemoryEntry;
import v9t9.common.memory.IMemoryWriteListener;

/**
 * Track a series of changes to a memory domain
 * @author ejs
 *
 */
public class MemoryWriteTracker {
	final IMemoryDomain domain;
	final byte[] mirror;
	BitSet changedMemory = new BitSet();
	private IMemoryWriteListener memoryWriteListener;
	
	public MemoryWriteTracker(IMemoryDomain domain) {
		this.domain = domain;
		mirror = new byte[65536];
	}
	public void addMemoryListener() {
		if (memoryWriteListener == null) {
			memoryWriteListener = new IMemoryWriteListener() {
	
				public void changed(IMemoryEntry entry, int addr, final boolean isByte) {
					synchronized (MemoryWriteTracker.this) {
						synchronized (changedMemory) {
							changedMemory.set(addr);
							mirror[addr] = entry.flatReadByte(addr);
							if (!isByte) {
								changedMemory.set(addr + 1);
								mirror[addr + 1] = entry.flatReadByte(addr);
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