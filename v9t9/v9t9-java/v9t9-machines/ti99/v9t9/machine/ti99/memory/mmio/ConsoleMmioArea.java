/**
 * 
 */
package v9t9.machine.ti99.memory.mmio;

import v9t9.engine.memory.WordMemoryArea;
import v9t9.engine.memory.ZeroWordMemoryArea;

public class ConsoleMmioArea extends WordMemoryArea {
    public ConsoleMmioArea() {
    	// the 16->8 bit multiplexer forces all accesses to be slow
    	this(4);
    	
    }
    public ConsoleMmioArea(int latency) {
    	super(latency);
        bWordAccess = true;
        memory = ZeroWordMemoryArea.zeroes;
    }
    
    @Override
    public boolean hasReadAccess() {
    	return false;
    }
}