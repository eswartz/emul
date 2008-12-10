/**
 * 
 */
package v9t9.emulator.hardware.memory;

import v9t9.engine.memory.ByteMemoryArea;

public class VdpRamArea extends ByteMemoryArea {
    public VdpRamArea(int size) {
    	// latency is counted in the CPU side;
    	// side effects are handled on the MMIO side
    	super(0);
        memory = new byte[size];
        read = memory;
        write = memory;
    }
    
}