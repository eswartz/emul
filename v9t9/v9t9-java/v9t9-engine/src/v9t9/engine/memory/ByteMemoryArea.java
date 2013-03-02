/*
  ByteMemoryArea.java

  (c) 2008-2011 Edward Swartz

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
package v9t9.engine.memory;

import v9t9.common.memory.ByteMemoryAccess;
import v9t9.common.memory.IMemoryEntry;

/**
 * Byte memory decomposes word accesses into two byte accesses in big-endian order.
 * <p>
 * This area provides a basis for "traditional" memory which has a flat
 * array of bytes with read and/or write support.
 *
 * @author ejs
 */
public class ByteMemoryArea extends MemoryArea {
	public ByteMemoryArea() {
		super(0);
	}
	public ByteMemoryArea(int latency) {
		super(latency);
	}
    public ByteMemoryArea(int latency, byte[] memory) {
		super(latency);
		this.memory = memory;
		this.read = memory;
		this.write = memory;
	}
    
    
	/* actual memory for area, except for empty mem */
    public byte[] memory;

    /* if non-NULL, we can statically read */
    public byte[] read;

    /* if non-NULL, we can statically write */
    public byte[] write;

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
	public boolean equals(Object obj) {
        if (!(obj instanceof ByteMemoryArea)) {
			return false;
		}
        ByteMemoryArea area = (ByteMemoryArea)obj;
        return area.memory == memory && area.read == read && area.write == write;
    }

    @Override
	public boolean hasWriteAccess() {
        return read != null && write != null;
    }

    @Override
	public boolean hasReadAccess() {
        return read != null;
    }

    @Override
	protected int getSize() {
        return memory != null ? memory.length : 0;
    }
    
    @Override
	public
	void copyFromBytes(byte[] array) {
    	if (memory == null) {
    		memory = new byte[array.length];
    		read = memory;
    	} 
        System.arraycopy(array, 0, memory, 0, Math.min(array.length, memory.length));
    }

    @Override
	public
	void copyToBytes(byte[] array) {
        System.arraycopy(memory, 0, array, 0, Math.min(array.length, memory.length));
    }

    @Override
	public short readWord(IMemoryEntry entry, int addr) {
		if (addr + 1 - entry.getAddr() < entry.getSize())
			return (short) ((readByte(entry, addr) << 8) | (readByte(entry, (addr + 1) & 0xffff) & 0xff));
		else
			return (short) ((readByte(entry, addr) << 8));
    }

    @Override
	public byte readByte(IMemoryEntry entry, int addr) {
        if (read != null) {
			return read[addr - entry.getAddr()];
		} else {
			return 0;
		}
    }

    @Override
	public void writeWord(IMemoryEntry entry, int addr, short val) {
    	writeByte(entry, addr, (byte) (val >> 8));
    	if (addr + 1 - entry.getAddr() < entry.getSize())
    		writeByte(entry, addr + 1, (byte) (val & 0xff));
    }

    @Override
	public void writeByte(IMemoryEntry entry, int addr, byte val) {
        if (write != null) {
			write[addr - entry.getAddr()] = val;
		}
    }

    public ByteMemoryAccess getReadMemoryAccess(IMemoryEntry entry, int addr) {
    	return new ByteMemoryAccess(read, addr - entry.getAddr());
    }
}