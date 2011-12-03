/**
 * 
 */
package v9t9.machine.ti99.memory.mmio;

import v9t9.engine.memory.VdpMmio;



public class ConsoleVdpReadArea extends ConsoleMmioReadArea {
	
    public ConsoleVdpReadArea(VdpMmio mmio) {
        super(mmio);
    }
    
    @Override
    public byte getLatency() {
    	return 5;
    }
}