/**
 * 
 */
package v9t9.machine.ti99.memory;

import v9t9.engine.memory.WordMemoryArea;

/** Memory hardwired into the console */
public class ConsoleMemoryArea extends WordMemoryArea {
    public ConsoleMemoryArea(int latency) {
    	super(latency);
    }
}