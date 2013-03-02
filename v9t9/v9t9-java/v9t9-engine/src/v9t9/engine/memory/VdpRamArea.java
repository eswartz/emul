/*
  VdpRamArea.java

  (c) 2008-2012 Edward Swartz

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

import v9t9.common.memory.IMemoryEntry;

public class VdpRamArea extends ByteMemoryArea {
	public VdpRamArea(int size) {
    	// latency is counted in the CPU side;
    	// side effects are handled on the MMIO side
    	super(0);
        memory = new byte[size];
        read = memory;
        write = memory;
    }
	
	/* (non-Javadoc)
	 * @see v9t9.engine.memory.MemoryArea#flatWriteByte(v9t9.common.memory.IMemoryEntry, int, byte)
	 */
	@Override
	public void flatWriteByte(IMemoryEntry entry, int addr, byte val) {
		super.writeByte(entry, addr, val);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.memory.MemoryArea#flatWriteWord(v9t9.common.memory.IMemoryEntry, int, short)
	 */
	@Override
	public void flatWriteWord(IMemoryEntry entry, int addr, short val) {
		super.writeWord(entry, addr, val);
	}
    @Override
    public void writeByte(IMemoryEntry entry, int addr, byte val) {
    	byte old = readByte(entry, addr);
    	if (old != val) {
    		super.writeByte(entry, addr, val);
    	}
    }
    
    @Override
    public byte readByte(IMemoryEntry entry, int addr) {
    	return super.readByte(entry, addr);
    }
}