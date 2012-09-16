/**
 * 
 */
package v9t9.gui.client.swt.shells.debugger;

import java.util.BitSet;

import v9t9.common.memory.IMemory;
import v9t9.common.memory.IMemoryDomain;
import v9t9.common.memory.IMemoryEntry;
import v9t9.common.memory.SimpleMemoryWriteTracker;

/**
 * A range of viewable memory
 * @author ejs
 *
 */
public class MemoryRange {
	final IMemoryEntry entry;
	final int addr;
	final int len;
	private SimpleMemoryWriteTracker tracker;
	protected int lowRange;
	protected int hiRange;
	private BitSet changes;
	
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
		tracker = new SimpleMemoryWriteTracker(entry.getDomain(), 0);
		tracker.addMemoryRange(addr, len);
	}
	public MemoryRange(IMemoryEntry element) {
		this.entry = element;
		this.addr = element.getAddr();
		this.len = element.getSize();
		tracker = new SimpleMemoryWriteTracker(entry.getDomain(), 0);
		tracker.addMemoryRange(addr, len);
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
		tracker.addMemoryListener();
		clearTouchRange();
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
			entry.writeByte(addr, byt);	
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
			boolean f = changes.get(addr) || (entry.isWordAccess() && changes.get(addr+1));
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
	public synchronized void clearTouchRange() {
		lowRange = Integer.MAX_VALUE;
		hiRange = 0;
	}
	public void removeMemoryListener() {
		tracker.removeMemoryListener();
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