/**
 * 
 */
package v9t9.engine.memory;

import java.io.IOException;

import v9t9.engine.memory.MemoryArea.AreaWriteByte;

/**
 * @author ejs
 *
 */
public class BankedMemoryEntry extends MemoryEntry {
	
	private class BankTogglingAreaWriteByte implements AreaWriteByte {

		private BankedMemoryEntry entry;
		BankTogglingAreaWriteByte(BankedMemoryEntry entry) {
			this.entry = entry;
		}
		public void writeByte(MemoryArea area, int address, byte val) {
			int bank = (address & 2) >> 1;
			entry.selectBank(bank);
		}
	}
	
	static public BankedMemoryEntry newBankedWordMemoryFromFile(
			Memory memory,
            String name, 
            MemoryDomain domain, 
            String filepath, int fileoffs,
            String filepath2, int fileoffs2) throws IOException {
    	DiskMemoryEntry bank0 = DiskMemoryEntry.newFromFile(
    			new WordMemoryArea(), 0x6000, 0x2000, name + " (bank 0)", domain, filepath, fileoffs, false);
    	DiskMemoryEntry bank1 = DiskMemoryEntry.newFromFile(
    			new WordMemoryArea(), 0x6000, 0x2000, name + " (bank 1)", domain, filepath2, fileoffs2, false);
    	
    	return new BankedMemoryEntry(memory, name, domain, bank0, bank1);
    }

	private MemoryEntry banks[];
	private MemoryEntry currentBank;
	private Memory memory;

	public BankedMemoryEntry(Memory memory,
			String name, MemoryDomain domain,
			MemoryEntry bank0, MemoryEntry bank1) {
		super(name, domain, 0x6000, 0x2000, bank0.area);
		
		this.memory = memory;
		
		this.banks = new MemoryEntry[2];
		this.banks[0] = bank0;
		this.banks[1] = bank1;
		
    	bank0.area.areaWriteWord = null;
    	bank0.area.areaWriteByte = new BankTogglingAreaWriteByte(this);
    	bank1.area.areaWriteWord = null;
    	bank1.area.areaWriteByte = new BankTogglingAreaWriteByte(this);

		this.currentBank = null;
		selectBank(0);
	}
	
	public void selectBank(int bank) {
		if (currentBank == null || currentBank != banks[bank]) {
			if (currentBank != null) {
				memory.notifyListeners(currentBank);
			}
			currentBank = banks[bank];
			currentBank.map();
			if (currentBank != null) {
				memory.notifyListeners(currentBank);
			}
		}
	}

	

}
