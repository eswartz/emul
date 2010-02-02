/**
 * 
 */
package v9t9.emulator.hardware.memory.mmio;

import v9t9.emulator.Machine.ConsoleMmioReader;
import v9t9.engine.memory.MemoryEntry;

public class ConsoleMmioReadArea extends ConsoleMmioArea {
    protected final ConsoleMmioReader reader;

	public ConsoleMmioReadArea(ConsoleMmioReader reader) {
        this.reader = reader;
		if (reader == null) {
			throw new NullPointerException();
		}
    }
	
	@Override
	public byte readByte(MemoryEntry entry, int addr) {
		if (0 == (addr & 1))
			return reader.read(addr);
		return 0;
	}
	
	@Override
	public short readWord(MemoryEntry entry, int addr) {
		return reader.read(addr);
	}
	
	@Override
	public byte flatReadByte(MemoryEntry entry, int addr) {
		return 0;
	}
	
	@Override
	public short flatReadWord(MemoryEntry entry, int addr) {
		return 0;
	}
	
	@Override
	public void flatWriteByte(MemoryEntry entry, int addr, byte val) {
	}
	
	@Override
	public void flatWriteWord(MemoryEntry entry, int addr, short val) {
	}
}