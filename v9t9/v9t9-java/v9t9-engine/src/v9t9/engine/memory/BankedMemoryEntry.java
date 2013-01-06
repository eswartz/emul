/**
 * 
 */
package v9t9.engine.memory;

import ejs.base.settings.ISettingSection;
import v9t9.common.memory.IMemory;
import v9t9.common.memory.IMemoryDomain;




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

	/**
	 * Only use when reconstructing 
	 */
	public BankedMemoryEntry() {
	}
	public BankedMemoryEntry(IMemory memory,
			String name,
			IMemoryDomain domain,
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
		doSelectBank(currentBankIndex);
	}
	
	public boolean selectBank(int bank) {
		bank &= bankCount - 1;
		if (currentBankIndex != bank) {
			doSelectBank(bank);
			return true;
		}
		return false;
	}
	protected void doSelectBank(int bank) {
		doSwitchBank(bank);
		currentBankIndex = bank;
		getMemory().notifyListenersOfLogicalChange(this);
		currentBankIndex = bank;
	}
	
	abstract protected void doSwitchBank(int bank);

	public int getCurrentBank() {
		return currentBankIndex;
	}

	public int getBankCount() {
		return bankCount;
	}
	
	public int getBankSize() {
		return getSize();
	}
	
	@Override
	public void saveState(ISettingSection section) {
		super.saveState(section);
		section.put("CurrentBankIndex", currentBankIndex);
		
		doSaveBankEntries(section.addSection("Banks"));
	}
	

	@Override
	public void loadState(ISettingSection section) {
		super.loadState(section);

		doLoadBankEntries(section.getSection("Banks"));
		
		selectBank(section.getInt("CurrentBankIndex"));
		
	}
	
	abstract protected void doSaveBankEntries(ISettingSection section);
	abstract protected void doLoadBankEntries(ISettingSection section);

	@Override
	protected void saveMemoryContents(ISettingSection section) {
		// do this per-bank
	}
	
	@Override
	protected void loadMemoryContents(ISettingSection section) {
		// do this per-bank
	}
	

	
	@Override
	public String getUniqueName() {
		return super.getUniqueName() + " #" + currentBankIndex + "";
	}
}
