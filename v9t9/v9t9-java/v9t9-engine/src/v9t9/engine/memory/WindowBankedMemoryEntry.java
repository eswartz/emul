/*
  WindowBankedMemoryEntry.java

  (c) 2008-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.memory;

import ejs.base.settings.ISettingSection;
import v9t9.common.memory.IMemory;
import v9t9.common.memory.IMemoryDomain;

/**
 * This is banked memory which exposes a portion of a larger
 * MemoryArea based on the current bank.
 * @author ejs
 *
 */
public class WindowBankedMemoryEntry extends BankedMemoryEntry {

	public WindowBankedMemoryEntry(IMemory memory, String name,
			IMemoryDomain domain, int addr, int size,
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
		addrOffset = (bank * getBankSize());
	}

	//@Override
	//protected int mapAddress(int addr) {
	//	return (addr & (size - 1)) + bankOffset;
	//}
	
	public int getBankOffset() {
		return getAddrOffset();
	}
	
	@Override
	protected void doSaveBankEntries(ISettingSection section) {
		area.saveContents(section);
	}
	
	@Override
	protected void doLoadBankEntries(ISettingSection section) {
		if (section == null) return;
		area.loadContents(section);
	}
	
	@Override
	public boolean contains(int addr) {
		int base = getCurrentBank() * getBankSize();
		return (addr >= base 
				&& addr < base + getBankSize());
	}
}
