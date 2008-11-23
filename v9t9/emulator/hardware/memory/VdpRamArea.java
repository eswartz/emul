/**
 * 
 */
package v9t9.emulator.hardware.memory;

import v9t9.engine.memory.ByteMemoryArea;

public class VdpRamArea extends ByteMemoryArea {
    public VdpRamArea() {
    	// latency is counted in the CPU side
    	super(0);
        memory = new byte[0x4000];
        read = memory;
        write = memory;
        
    }
}