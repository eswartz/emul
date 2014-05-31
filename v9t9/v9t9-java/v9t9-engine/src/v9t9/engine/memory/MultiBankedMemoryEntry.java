/*
  MultiBankedMemoryEntry.java

  (c) 2008-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.memory;

import ejs.base.settings.ISettingSection;
import v9t9.common.client.ISettingsHandler;
import v9t9.common.memory.IMemory;
import v9t9.common.memory.IMemoryEntry;




/**
 * This is banked memory which exposes a distinct memory entry
 * based on the current bank.
 * @author ejs
 *
 */
public class MultiBankedMemoryEntry extends BankedMemoryEntry {
	
	private IMemoryEntry banks[];
	private IMemoryEntry currentBank;

	/**
	 * Only to be used when reconstructing 
	 */
	public MultiBankedMemoryEntry() {
	}
	public MultiBankedMemoryEntry(ISettingsHandler settings, IMemory memory,
			String name, IMemoryEntry[] banks) {
		super(memory, name, banks[0].getDomain(), banks[0].getAddr(), banks[0].getSize(), banks.length);
		
		this.banks = new MemoryEntry[banks.length];
		System.arraycopy(banks, 0, this.banks, 0, banks.length);
		
		this.currentBank = null;
		selectBank(0);
	}

	@Override
	protected void doSwitchBank(int bank) {
		currentBank = banks[bank % bankCount];
		setArea((MemoryArea) currentBank.getArea());
	}
	
	@Override
	public void load() {
		for (IMemoryEntry bank : banks)
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
	protected void doSaveBankEntries(ISettingSection section) {
		for (int idx = 0; idx < banks.length; idx++) {
			IMemoryEntry entry = banks[idx];
			entry.saveState(section.addSection("" + idx));
		}		
	}

	@Override
	protected void doLoadBankEntries(ISettingSection section) {
		if (section == null) return;
		if (banks == null) {
			bankCount = section.getSections().length;
			banks = new MemoryEntry[bankCount];
		}
		for (int idx = 0; idx < banks.length; idx++) {
			ISettingSection entryStore = section.getSection("" + idx);
			IMemoryEntry entry = banks[idx];
			if (entry != null) {
				((MemoryEntry) entry).setMemory(memory);
				entry.loadState(entryStore);
			} else {
				entry = memory.getMemoryEntryFactory().createEntry(getDomain(), entryStore);
				banks[idx] = entry;
			}
		}		
	}
	
	@Override
	public String getUniqueName() {
		return currentBank != null ? currentBank.getUniqueName() : super.getUniqueName();
	}
	
	public IMemoryEntry[] getBanks() {
		return banks;
	}
}
