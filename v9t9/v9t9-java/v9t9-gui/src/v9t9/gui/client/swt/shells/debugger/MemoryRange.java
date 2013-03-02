/*
  MemoryRange.java

  (c) 2009-2012 Edward Swartz

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
package v9t9.gui.client.swt.shells.debugger;

import v9t9.common.memory.IMemory;
import v9t9.common.memory.IMemoryDomain;
import v9t9.common.memory.IMemoryEntry;

/**
 * A range of viewable memory
 * @author ejs
 *
 */
public class MemoryRange {
	final IMemoryEntry entry;
	final int addr;
	final int len;
	
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