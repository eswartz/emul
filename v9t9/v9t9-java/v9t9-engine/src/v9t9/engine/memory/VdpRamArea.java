/*
  VdpRamArea.java

  (c) 2008-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.memory;


public class VdpRamArea extends ByteMemoryArea {
	public VdpRamArea(int size) {
    	// latency is counted in the CPU side;
    	// side effects are handled on the MMIO side
    	super(0);
        memory = new byte[size];
        read = memory;
        write = memory;
    }
	
	@Override
	public void flatWriteByte(int addr, byte val) {
		super.writeByte(addr, val);
	}
	
	@Override
	public void flatWriteWord(int addr, short val) {
		super.writeWord(addr, val);
	}
    @Override
    public void writeByte(int addr, byte val) {
    	byte old = readByte(addr);
    	if (old != val) {
    		super.writeByte(addr, val);
    	}
    }
    
    @Override
    public byte readByte(int addr) {
    	return super.readByte(addr);
    }
}