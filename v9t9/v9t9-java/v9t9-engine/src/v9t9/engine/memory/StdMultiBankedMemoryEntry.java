/*
  StdMultiBankedMemoryEntry.java

  (c) 2010-2013 Edward Swartz

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

import java.io.PrintWriter;

import ejs.base.properties.IProperty;
import ejs.base.settings.Logging;


import v9t9.common.client.ISettingsHandler;
import v9t9.common.cpu.ICpu;
import v9t9.common.memory.IMemory;
import v9t9.common.memory.IMemoryEntry;

/**
 * This is a standard TI-99/4A style banked memory entry, where a write to
 * a byte/word in increments of >2 from >6000 will select the given bank.
 * @author ejs
 *
 */
public class StdMultiBankedMemoryEntry extends
		MultiBankedMemoryEntry {
	private IProperty dumpFullInstructions;

	/**
	 * Only to be used when reconstructing 
	 */
	public StdMultiBankedMemoryEntry() {
	}
	/**
	 * @param memory
	 * @param name
	 * @param banks
	 */
	public StdMultiBankedMemoryEntry(ISettingsHandler settings, IMemory memory, String name,
			IMemoryEntry[] banks) {
		super(settings, memory, name, banks);
		dumpFullInstructions = settings.get(ICpu.settingDumpFullInstructions);
	}

	@Override
	public boolean isStatic() {
		return false;
	}
	
	@Override
	public void writeByte(int addr, byte val) {
		int bank = (addr & 0xff) >> 1;
		if (selectBank(bank)) {
			
			PrintWriter log = Logging.getLog(dumpFullInstructions);
			if (log != null) {
				log.println("=== Switched to bank " + bank);
			}
		}
		super.writeByte(addr, val);
	}

	@Override
	public void writeWord(int addr, short val) {
		int bank = (addr & 0xff) >> 1;
		if (selectBank(bank)) {
			PrintWriter log = Logging.getLog(dumpFullInstructions);
			if (log != null) {
				log.println("=== Switched to bank " + bank);
			}
		}
		super.writeWord(addr, val);
	}
}