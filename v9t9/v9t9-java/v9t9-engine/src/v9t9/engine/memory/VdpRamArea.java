/*
  VdpRamArea.java

  (c) 2008-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
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