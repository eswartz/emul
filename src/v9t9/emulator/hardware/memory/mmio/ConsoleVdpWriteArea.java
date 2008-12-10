/**
 * 
 */
package v9t9.emulator.hardware.memory.mmio;



public class ConsoleVdpWriteArea extends ConsoleMmioWriteArea {
    public ConsoleVdpWriteArea(VdpMmio mmio) {
        super(mmio);
    }
    
    @Override
    public byte getWriteByteLatency() {
    	return (byte) (4 + ((VdpMmio)writer).getMemoryAccessCycles());
    }

}