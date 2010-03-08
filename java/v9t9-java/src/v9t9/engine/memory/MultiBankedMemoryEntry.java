/**
 * 
 */
package v9t9.engine.memory;

import org.eclipse.jface.dialogs.IDialogSettings;




/**
 * This is banked memory which exposes a distinct memory entry
 * based on the current bank.
 * @author ejs
 *
 */
public class MultiBankedMemoryEntry extends BankedMemoryEntry {
	
	private MemoryEntry banks[];
	private MemoryEntry currentBank;

	/**
	 * Only to be used when reconstructing 
	 */
	public MultiBankedMemoryEntry() {
	}
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
	
	//@Override
	//public MemoryArea getArea() {
	//	return currentBank.getArea();
	//}

	@Override
	protected void doSaveBankEntries(IDialogSettings section) {
		for (int idx = 0; idx < banks.length; idx++) {
			MemoryEntry entry = banks[idx];
			entry.saveState(section.addNewSection("" + idx));
		}		
	}

	@Override
	protected void doLoadBankEntries(IDialogSettings section) {
		if (section == null) return;
		for (int idx = 0; idx < banks.length; idx++) {
			MemoryEntry entry = banks[idx];
			entry.loadState(section.getSection("" + idx));
		}		
	}
	
	@Override
	public String getUniqueName() {
		return currentBank != null ? currentBank.getUniqueName() : super.getUniqueName();
	}
	
	public MemoryEntry[] getBanks() {
		return banks;
	}
}
