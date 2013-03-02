/*
  BankedMemoryEntry.java

  (c) 2008-2013 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
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
		if (bank >= bankCount)
			return false;
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
