/**
 * 
 */
package v9t9.engine.memory;

import org.eclipse.jface.dialogs.IDialogSettings;




/**
 * Banked memory exposes different contents based on the
 * current bank.  The concrete implementation reacts to
 * bank changes via {@link #doSwitchBank(int)} and may override
 * other MemoryEntry methods accordingly.
 * @author ejs
 *
 */
public abstract class BankedMemoryEntry extends MemoryEntry {
	
	private int currentBankIndex;
	protected int bankCount;
	private Memory memory;

	/**
	 * Only use when reconstructing 
	 */
	public BankedMemoryEntry() {
	}
	public BankedMemoryEntry(Memory memory,
			String name,
			MemoryDomain domain,
			int addr,
			int size,
			int bankCount) {
		super(name, domain, addr, size, null);
		
		this.memory = memory;
		this.currentBankIndex = -1;
		this.bankCount = bankCount;
	}
	
	@Override
	public void onMap() {
		super.onMap();
		selectBank(currentBankIndex);
	}
	
	public boolean selectBank(int bank) {
		if (currentBankIndex != bank) {
			doSwitchBank(bank);
			currentBankIndex = bank;
			memory.notifyListenersOfLogicalChange(this);
			currentBankIndex = bank;
			return true;
		}
		return false;
	}
	
	abstract protected void doSwitchBank(int bank);

	public int getCurrentBank() {
		return currentBankIndex;
	}

	public int getBankCount() {
		return bankCount;
	}

	/*
	@Override
	public void writeByte(int addr, byte val) {
		int bank = (addr & 2) >> 1;
		selectBank(bank);
		super.writeByte(addr, val);
	}
	
	@Override
	public void writeWord(int addr, short val) {
		int bank = (addr & 2) >> 1;
		selectBank(bank);
		super.writeWord(addr, val);
	}
	*/
	
	public int getBankSize() {
		return size;
	}
	
	@Override
	public void saveState(IDialogSettings section) {
		super.saveState(section);
		section.put("CurrentBankIndex", currentBankIndex);
		
		doSaveBankEntries(section.addNewSection("Banks"));
	}
	
	abstract protected void doSaveBankEntries(IDialogSettings section);

	@Override
	protected void saveMemoryContents(IDialogSettings section) {
		// do this per-bank
	}
	
	@Override
	protected void loadMemoryContents(IDialogSettings section) {
		// do this per-bank
	}
	
	@Override
	public void loadState(IDialogSettings section) {
		super.loadState(section);

		doLoadBankEntries(section.getSection("Banks"));
		
		selectBank(section.getInt("CurrentBankIndex"));
		
	}

	abstract protected void doLoadBankEntries(IDialogSettings section);
	
	@Override
	public String getUniqueName() {
		return super.getUniqueName() + " #" + currentBankIndex + "";
	}
}
