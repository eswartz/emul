/**
 * 
 */
package v9t9.engine.memory;

import v9t9.engine.memory.MemoryArea.AreaWriteByte;

public class BankTogglingAreaWriteByte implements AreaWriteByte {

	private BankedMemoryEntry entry;
	BankTogglingAreaWriteByte(BankedMemoryEntry entry) {
		this.entry = entry;
	}
	public void writeByte(MemoryArea area, int address, byte val) {
		int bank = (address & 2) >> 1;
		entry.selectBank(bank);
	}
}