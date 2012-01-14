/**
 * 
 */
package v9t9.gui.client.swt.shells.debugger;

import java.util.Set;
import java.util.TreeSet;

import v9t9.common.memory.IMemory;
import v9t9.common.memory.IMemoryDomain;
import v9t9.common.memory.IMemoryEntry;
import v9t9.common.memory.IMemoryWriteListener;

/**
 * A range of viewable memory
 * @author ejs
 *
 */
public class MemoryRange {
	final IMemoryEntry entry;
	final int addr;
	final int len;
	Set<Integer> changedMemory = new TreeSet<Integer>();
	private IMemoryWriteListener memoryWriteListener;
	protected int lowRange;
	protected int hiRange;
	
	public MemoryRange(IMemoryEntry entry, int addr, int len) {
		this.entry = entry;
		int lo = addr;
		int hi = addr + len;
		if (entry.getAddr() > lo)
			lo = entry.getAddr();
		if (entry.getAddr() + entry.getSize() < hi)
			hi = entry.getAddr() + entry.getSize() - lo; 
		this.addr = lo;
		this.len = hi - lo;
	}
	public MemoryRange(IMemoryEntry element) {
		this.entry = element;
		this.addr = element.getAddr();
		this.len = element.getSize();
	}
	public boolean contains(IMemoryEntry entry, int addr) {
		return this.entry.getDomain() == entry.getDomain() &&
			this.entry.contains(addr) &&
			addr >= this.addr && addr < this.addr + this.len;
	}
	public boolean isWordMemory() {
		return entry.isWordAccess();
	}
	public void attachMemoryListener() {
		memoryWriteListener = new IMemoryWriteListener() {

			public void changed(IMemoryEntry entry, int addr, boolean isByte) {
				synchronized (MemoryRange.this) {
					if (contains(entry, addr)) {
						lowRange = Math.min(lowRange, addr);
						hiRange = Math.max(hiRange, addr);
						
						synchronized (changedMemory) {
							changedMemory.add(addr);
							if (isWordMemory()) {
								changedMemory.add(addr + 1);
							}
						}
					}
				}
			}
			
		};
		
		clearTouchRange();
		synchronized (changedMemory) {
			changedMemory.clear();
		}
		entry.getDomain().addWriteListener(memoryWriteListener);
	}
	public int getAddress() {
		return addr;
	}
	public int readByte(int addr) {
		if (addr >= this.addr && addr < this.addr + this.len)
			return entry.flatReadByte(addr);
		else
			return 0;
	}
	public void writeByte(int addr, byte byt) {
		if (addr >= this.addr && addr < this.addr + this.len)
			entry.flatWriteByte(addr, byt);	
	}
	public boolean getAndResetChanged(Integer addr) {
		synchronized (changedMemory) {
			boolean changed = changedMemory.remove(addr);
			return changed;
		}
	}
	public synchronized int getLowTouchRange() {
		return lowRange;
	}
	public synchronized int getHiTouchRange() {
		return hiRange;
	}
	public synchronized void clearTouchRange() {
		lowRange = Integer.MAX_VALUE;
		hiRange = 0;
	}
	public void removeMemoryListener() {
		entry.getDomain().removeWriteListener(memoryWriteListener);
	}
	public int getSize() {
		return len;
	}
	/**
	 * @param i  
	 */
	public boolean canModify(int i) {
		return entry.hasWriteAccess();
	}
	public IMemoryEntry getEntry() {
		return entry;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return entry.getDomain().getIdentifier() + "|" + addr + "|" + len;
	}
	/**
	 * @param range
	 * @return
	 */
	public static MemoryRange fromString(IMemory memory, String range) {
		if (range == null)
			return null;
		String[] parts = range.split("\\|");
		if (parts.length != 3)
			return null;
		IMemoryDomain domain = memory.getDomain(parts[0]);
		if (domain == null)
			return null;
		try {
			int addr = Integer.parseInt(parts[1]);
			int len = Integer.parseInt(parts[2]);
			IMemoryEntry entry = domain.getEntryAt(addr);
			if (entry == null)
				return null;
			return new MemoryRange(entry, addr, len);
		} catch (NumberFormatException e) {
			return null;
		}
	}
	
}