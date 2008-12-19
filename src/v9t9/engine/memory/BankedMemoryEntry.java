/**
 * 
 */
package v9t9.engine.memory;




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
			memory.notifyListeners(this);
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
}
