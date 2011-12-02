/**
 * 
 */
package v9t9.emulator.hardware.memory.mmio;

import v9t9.emulator.common.Machine.ConsoleMmioWriter;
import v9t9.engine.memory.MemoryEntry;

public class ConsoleMmioWriteArea extends ConsoleMmioArea {
    protected final ConsoleMmioWriter writer;

	ConsoleMmioWriteArea(ConsoleMmioWriter writer) {
        this.writer = writer;
		if (writer == null) {
			throw new NullPointerException();
		}
    };
    
    @Override
    public void writeByte(MemoryEntry entry, int addr, byte val) {
    	if (0 == (addr & 1))
    		writer.write(addr, val);
    }
    
    @Override
    public void writeWord(MemoryEntry entry, int addr, short val) {
    	writer.write(addr, (byte) (val >> 8));
    }
}