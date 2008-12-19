/**
 * 
 */
package v9t9.engine.memory;




/**
 * Memory that supports multiple banks.
 * @author ejs
 *
 */
public class BankedMemoryEntry extends MemoryEntry {
	
	private MemoryEntry banks[];
	private MemoryEntry currentBank;
	private Memory memory;
	private int currentBankIndex;

	public BankedMemoryEntry(Memory memory,
			String name, MemoryEntry[] banks) {
		super(name, banks[0].domain, banks[0].addr, banks[0].size, banks[0].area);
		
		this.memory = memory;
		
		this.banks = new MemoryEntry[banks.length];
		System.arraycopy(banks, 0, this.banks, 0, banks.length);
		
		this.currentBank = null;
		this.currentBankIndex = 0;
	}
	
	@Override
	public void onMap() {
		super.onMap();
		if (currentBank == null)
			selectBank(currentBankIndex);
	}
	
	public void selectBank(int bank) {
		MemoryEntry newBankEntry = banks[bank];
		if (currentBank == null || currentBank != newBankEntry) {
			if (currentBank != null) {
				memory.notifyListeners(currentBank);
			//	currentBank.domain.unmapEntry(currentBank);
			}
			//domain.mapEntry(newBankEntry);
			domain.switchBankedEntry(currentBank, newBankEntry);
			currentBank = newBankEntry;
			if (currentBank != null) {
				memory.notifyListeners(currentBank);
			}
			currentBankIndex = bank;
		}
	}
	
	public MemoryEntry getCurrentBankEntry() {
		return currentBank;
	}
	
	public int getCurrentBank() {
		return currentBankIndex;
	}

	public MemoryEntry getBank(int i) {
		return banks[i];
	}

	public int getBankCount() {
		return banks.length;
	}

	@Override
	public String lookupSymbol(short addr) {
		if (currentBank == null)
			return null;
		return currentBank.lookupSymbol(addr);
	}

}
