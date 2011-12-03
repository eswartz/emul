/**
 * 
 */
package v9t9.emulator.hardware.memory.mmio;



public class ConsoleVdpReadArea extends ConsoleMmioReadArea {
	
    public ConsoleVdpReadArea(VdpMmio mmio) {
        super(mmio);
    }
    
    @Override
    public byte getLatency() {
    	return 5;
    }
}