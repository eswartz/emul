/**
 * 
 */
package v9t9.emulator.hardware.memory;

import v9t9.engine.memory.WordMemoryArea;

/** Memory accessed over the 99/4A peripheral bus */
public class ExternalMemoryArea extends WordMemoryArea {
    public ExternalMemoryArea(int latency) {
    	super(latency);
        bWordAccess = true;
    }
}