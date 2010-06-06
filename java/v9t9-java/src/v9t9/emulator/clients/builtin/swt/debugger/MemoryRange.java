/**
 * 
 */
package v9t9.emulator.clients.builtin.swt.debugger;

import java.util.Set;
import java.util.TreeSet;

import v9t9.engine.memory.MemoryDomain;
import v9t9.engine.memory.MemoryEntry;
import v9t9.engine.memory.WordMemoryArea;
import v9t9.engine.memory.MemoryDomain.MemoryWriteListener;

/**
 * A range of viewable memory
 * @author ejs
 *
 */
public class MemoryRange {
	final MemoryEntry entry;
	final int addr;
	final int len;
	Set<Integer> changedMemory = new TreeSet<Integer>();
	private MemoryWriteListener memoryWriteListener;
	protected int lowRange;
	protected int hiRange;
	
	public MemoryRange(MemoryEntry entry, int addr, int len) {
		this.entry = entry;
		int lo = addr;
		int hi = addr + len;
		if (entry.addr > lo)
			lo = entry.addr;
		if (entry.addr + entry.size < hi)
			hi = entry.addr + entry.size - lo; 
		this.addr = lo;
		this.len = hi - lo;
	}
	public MemoryRange(MemoryEntry entry) {
		this.entry = entry;
		this.addr = entry.addr;
		this.len = entry.size;
	}
	public boolean contains(MemoryEntry entry, int addr) {
		return this.entry.domain == entry.domain &&
			this.entry.contains(addr) &&
			addr >= this.addr && addr < this.addr + this.len;
	}
	public boolean isWordMemory() {
		return entry.getArea() instanceof WordMemoryArea;
	}
	public void attachMemoryListener() {
		memoryWriteListener = new MemoryDomain.MemoryWriteListener() {

			public void changed(MemoryEntry entry, int addr, boolean isByte) {
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
		entry.domain.addWriteListener(memoryWriteListener);
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
		lowRange = 0xfffff;
		hiRange = 0;
	}
	public void removeMemoryListener() {
		entry.domain.removeWriteListener(memoryWriteListener);
	}
	public int getSize() {
		return len;
	}
	public boolean canModify(int i) {
		return entry.hasWriteAccess();
	}
	public MemoryEntry getEntry() {
		return entry;
	}
	
}