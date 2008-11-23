/**
 * 
 */
package v9t9.engine.memory;

import java.io.IOException;
import java.io.PrintWriter;

import v9t9.engine.memory.MemoryArea.AreaWriteByte;

/**
 * @author ejs
 *
 */
public class DualBankedMemoryEntry extends MemoryEntry {
	
	private class BankTogglingAreaWriteByte implements AreaWriteByte {

		private DualBankedMemoryEntry entry;
		BankTogglingAreaWriteByte(DualBankedMemoryEntry entry) {
			this.entry = entry;
		}
		public void writeByte(MemoryArea area, int address, byte val) {
			int bank = (address & 2) >> 1;
			entry.selectBank(bank);
		}
	}
	
	static public DualBankedMemoryEntry newBankedWordMemoryFromFile(
			int addr,
            int size, 
            Memory memory, 
            String name, MemoryDomain domain,
            String filepath, int fileoffs,
            String filepath2, int fileoffs2) throws IOException {
    	DiskMemoryEntry bank0 = DiskMemoryEntry.newFromFile(
    			new WordMemoryArea(domain.getReadWordLatency(addr)), 
    			addr, size, name + " (bank 0)", domain, filepath, fileoffs, false);
    	DiskMemoryEntry bank1 = DiskMemoryEntry.newFromFile(
    			new WordMemoryArea(domain.getReadWordLatency(addr)), 
    			addr, size, name + " (bank 1)", domain, filepath2, fileoffs2, false);
    	
    	return new DualBankedMemoryEntry(memory, name, domain, addr, size, bank0, bank1);
    }

	private MemoryEntry banks[];
	private MemoryEntry currentBank;
	private Memory memory;

	public DualBankedMemoryEntry(Memory memory,
			String name, MemoryDomain domain,
			int addr, int size,
			MemoryEntry bank0, MemoryEntry bank1) {
		super(name, domain, addr, size, bank0.area);
		
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
