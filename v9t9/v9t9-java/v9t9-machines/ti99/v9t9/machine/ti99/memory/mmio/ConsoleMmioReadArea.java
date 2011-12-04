/**
 * 
 */
package v9t9.machine.ti99.memory.mmio;

import v9t9.common.memory.IMemoryEntry;
import v9t9.engine.memory.IConsoleMmioReader;

public class ConsoleMmioReadArea extends ConsoleMmioArea {
    protected final IConsoleMmioReader reader;

	public ConsoleMmioReadArea(IConsoleMmioReader reader) {
        this.reader = reader;
		if (reader == null) {
			throw new NullPointerException();
		}
    }
	
	@Override
	public byte readByte(IMemoryEntry entry, int addr) {
		if (0 == (addr & 1))
			return reader.read(addr);
		return 0;
	}
	
	@Override
	public short readWord(IMemoryEntry entry, int addr) {
		return reader.read(addr);
	}
	
	@Override
	public byte flatReadByte(IMemoryEntry entry, int addr) {
		return 0;
	}
	
	@Override
	public short flatReadWord(IMemoryEntry entry, int addr) {
		return 0;
	}
	
	@Override
	public void flatWriteByte(IMemoryEntry entry, int addr, byte val) {
	}
	
	@Override
	public void flatWriteWord(IMemoryEntry entry, int addr, short val) {
	}
}