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
	public void map() {
		super.map();
		if (currentBank == null)
			selectBank(currentBankIndex);
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

	

}
