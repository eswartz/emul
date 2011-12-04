/**
 * 
 */
package v9t9.machine.f99b.memory;

import v9t9.common.memory.ByteMemoryArea;


/** Enhanced console RAM, byte-accessible */
public class EnhancedRomByteArea extends ByteMemoryArea {

    public EnhancedRomByteArea(int latency, int size) {
    	super(latency);
    	
        memory = new byte[size];
        read = memory;
        write = memory;
    }
}