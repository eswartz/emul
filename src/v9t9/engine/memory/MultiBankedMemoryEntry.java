/**
 * 
 */
package v9t9.engine.memory;




/**
 * This is banked memory which exposes a distinct memory entry
 * based on the current bank.
 * @author ejs
 *
 */
public class MultiBankedMemoryEntry extends BankedMemoryEntry {
	
	private MemoryEntry banks[];
	private MemoryEntry currentBank;

	public MultiBankedMemoryEntry(Memory memory,
			String name, MemoryEntry[] banks) {
		super(memory, name, banks[0].domain, banks[0].addr, banks[0].size, banks.length);
		
		this.banks = new MemoryEntry[banks.length];
		System.arraycopy(banks, 0, this.banks, 0, banks.length);
		
		this.currentBank = null;
		selectBank(0);
	}

	@Override
	protected void doSwitchBank(int bank) {
		currentBank = banks[bank % bankCount];
		setArea(currentBank.getArea());
	}
	
	@Override
	public void load() {
		for (MemoryEntry bank : banks)
			bank.load();
	}

	@Override
	public String lookupSymbol(short addr) {
		if (currentBank == null)
			return null;
		return currentBank.lookupSymbol(addr);
	}
	
	@Override
	protected MemoryArea getArea() {
		return currentBank.getArea();
	}
}
