package v9t9.common.memory;

import java.util.BitSet;


/**
 * Track a series of changes to a memory domain
 * @author ejs
 *
 */
public abstract class BaseMemoryWriteTracker {
	protected final IMemoryDomain domain;
	private final int granularityShift;
	private BitSet trackedMemory = new BitSet();
	private IMemoryWriteListener memoryWriteListener;
	
	/**
	 * Track changes to memory, allowing future changes at a granularity of 'granularityShift'
	 * @param domain
	 * @param granularityShift
	 */
	public BaseMemoryWriteTracker(IMemoryDomain domain, int granularityShift) {
		this.domain = domain;
		this.granularityShift = granularityShift;
	}
	
	public synchronized void addMemoryRange(int addr, int size) {
		addr >>>= granularityShift;
		if (granularityShift > 0)
			size = (size + ~(~0 >>> (32 - granularityShift)) - 1) >>> granularityShift;
			
		while (size > 0) {
			if (!trackedMemory.get(addr)) {
				trackedMemory.set(addr);
				recordChange(addr, null);
			}
			size--;
			addr++;
		}
	}
	
	public synchronized void removeMemoryRange(int addr, int size) {
		addr >>>= granularityShift;
		if (granularityShift > 0)
			size = (size + ~(~0 >>> (32 - granularityShift)) - 1) >>> granularityShift;
		trackedMemory.set(addr, addr + size, false);
	}
	
	public synchronized void addMemoryListener() {
		if (memoryWriteListener == null) {
			memoryWriteListener = new IMemoryWriteListener() {
	
				public void changed(IMemoryEntry entry, int addr, final Number value) {
					synchronized (BaseMemoryWriteTracker.this) {
						int xaddr = addr >>> granularityShift;
						if (trackedMemory.get(xaddr)) {
							recordChange(addr, value);
						}
					}
				}
				
			};
			
			clearChanges();
			domain.addWriteListener(memoryWriteListener);
		}
	}
	
	abstract protected void recordChange(int addr, Number val);

	abstract public void clearChanges();
	
	public synchronized void removeMemoryListener() {
		if (memoryWriteListener != null) {
			domain.removeWriteListener(memoryWriteListener);
			memoryWriteListener = null;
		}
	}
}