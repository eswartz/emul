/**
 * 
 */
package v9t9.machine.ti99.memory.mmio;

import v9t9.engine.memory.VdpMmio;



public class ConsoleVdpWriteArea extends ConsoleMmioWriteArea {
    public ConsoleVdpWriteArea(VdpMmio mmio) {
        super(mmio);
    }
    
    @Override
    public byte getLatency() {
    	return (byte) (4 + ((VdpMmio)writer).getMemoryAccessCycles());
    }

}