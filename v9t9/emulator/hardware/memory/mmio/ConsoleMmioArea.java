/**
 * 
 */
package v9t9.emulator.hardware.memory.mmio;

import v9t9.engine.memory.WordMemoryArea;
import v9t9.engine.memory.ZeroWordMemoryArea;

public class ConsoleMmioArea extends WordMemoryArea {
    public ConsoleMmioArea() {
    	// the 16->8 bit multiplexer forces all accesses to be slow
    	super(4);
        bWordAccess = true;
        memory = ZeroWordMemoryArea.zeroes;
    }
}