/**
 * 
 */
package v9t9.emulator.hardware.memory;

import v9t9.engine.memory.ByteMemoryArea;


/** Enhanced console RAM, byte-accessible */
public class EnhancedRamByteArea extends ByteMemoryArea {

    public EnhancedRamByteArea(int latency, int size) {
    	super(latency);
    	
        memory = new byte[size];
        read = memory;
        write = memory;
    }
}