/**
 * 
 */
package v9t9.engine.memory;

import v9t9.common.hardware.IVdpChip;
import v9t9.common.memory.IMemoryEntry;

public class VdpRamArea extends ByteMemoryArea {
    private IVdpChip handler;

	public VdpRamArea(int size) {
    	// latency is counted in the CPU side;
    	// side effects are handled on the MMIO side
    	super(0);
        memory = new byte[size];
        read = memory;
        write = memory;
    }
	
	public void setHandler(IVdpChip handler) {
		this.handler = handler;
	}
    
    @Override
    public void writeByte(IMemoryEntry entry, int addr, byte val) {
    	byte old = readByte(entry, addr);
    	if (old != val) {
    		super.writeByte(entry, addr, val);
    		if (handler != null) {
    			handler.touchAbsoluteVdpMemory(addr);
    		}

    	}
    }
    
    @Override
    public byte readByte(IMemoryEntry entry, int addr) {
    	return super.readByte(entry, addr);
    }
}