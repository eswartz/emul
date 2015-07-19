/*
  BankedMemoryEntry.java

  (c) 2008-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.memory;

import ejs.base.settings.ISettingSection;
import v9t9.common.events.IEventNotifier;
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
	
	/* (non-Javadoc)
	 * @see v9t9.engine.memory.MemoryEntry#reset()
	 */
	@Override
	public void reset() {
		super.reset();
		this.currentBankIndex = -1;
	}
	
	@Override
	public void onMap() {
		super.onMap();
		if (currentBankIndex < 0)
			currentBankIndex = 0;
		doSelectBank(currentBankIndex);
	}
	
	public boolean selectBank(int bank) {
		bank %= bankCount;
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
	
	/* (non-Javadoc)
	 * @see v9t9.engine.memory.MemoryEntry#loadMemory(v9t9.common.events.IEventNotifier, ejs.base.settings.ISettingSection)
	 */
	@Override
	public void loadMemory(IEventNotifier notifier, ISettingSection section) {
		super.loadMemory(notifier, section);
		
		doLoadBankEntries(notifier, section.getSection("Banks"));
		
		selectBank(section.getInt("CurrentBankIndex"));
	}
	
	abstract protected void doSaveBankEntries(ISettingSection section);
	abstract protected void doLoadBankEntries(IEventNotifier notifier, ISettingSection section);

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
