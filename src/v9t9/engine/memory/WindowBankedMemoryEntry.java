/**
 * 
 */
package v9t9.engine.memory;

import org.eclipse.jface.dialogs.IDialogSettings;

/**
 * This is banked memory which exposes a portion of a larger
 * MemoryArea based on the current bank.
 * @author ejs
 *
 */
public class WindowBankedMemoryEntry extends BankedMemoryEntry {

	private int bankOffset;

	public WindowBankedMemoryEntry(Memory memory, String name,
			MemoryDomain domain, int addr, int size,
			MemoryArea area) {
		super(memory, name, domain, addr, size, area.getSize() / size);
		selectBank(0);
		setArea(area);
		addrOffset = bankOffset;
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.memory.BankedMemoryEntry#doSwitchBank(int)
	 */
	@Override
	protected void doSwitchBank(int bank) {
		bankOffset = bank * getBankSize();
		addrOffset = bankOffset;
	}

	//@Override
	//protected int mapAddress(int addr) {
	//	return (addr & (size - 1)) + bankOffset;
	//}
	
	public int getBankOffset() {
		return bankOffset;
	}
	
	@Override
	protected void doSaveBankEntries(IDialogSettings section) {
		getArea().saveContents(section, this);
	}
	
	@Override
	protected void doLoadBankEntries(IDialogSettings section) {
		if (section == null) return;
		getArea().loadContents(section, this);
	}
}
