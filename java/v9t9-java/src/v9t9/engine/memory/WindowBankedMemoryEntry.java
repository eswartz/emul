/**
 * 
 */
package v9t9.engine.memory;

import org.ejs.coffee.core.properties.IPropertyStorage;

/**
 * This is banked memory which exposes a portion of a larger
 * MemoryArea based on the current bank.
 * @author ejs
 *
 */
public class WindowBankedMemoryEntry extends BankedMemoryEntry {

	public WindowBankedMemoryEntry(Memory memory, String name,
			MemoryDomain domain, int addr, int size,
			MemoryArea area) {
		super(memory, name, domain, addr, size, area.getSize() / size);
		selectBank(0);
		setArea(area);
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.memory.BankedMemoryEntry#doSwitchBank(int)
	 */
	@Override
	protected void doSwitchBank(int bank) {
		addrOffset = bank * getBankSize();
	}

	//@Override
	//protected int mapAddress(int addr) {
	//	return (addr & (size - 1)) + bankOffset;
	//}
	
	public int getBankOffset() {
		return addrOffset;
	}
	
	@Override
	protected void doSaveBankEntries(IPropertyStorage section) {
		getArea().saveContents(section, this);
	}
	
	@Override
	protected void doLoadBankEntries(IPropertyStorage section) {
		if (section == null) return;
		getArea().loadContents(section, this);
	}
	
	@Override
	public boolean contains(int addr) {
		int base = getCurrentBank() * getBankSize();
		return (addr >= base 
				&& addr < base + getBankSize());
	}
}
