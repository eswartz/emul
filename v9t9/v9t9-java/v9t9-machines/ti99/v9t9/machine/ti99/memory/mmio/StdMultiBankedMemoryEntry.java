/*
  StdMultiBankedMemoryEntry.java

  (c) 2010-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.machine.ti99.memory.mmio;

import java.io.PrintWriter;

import ejs.base.properties.IProperty;
import ejs.base.settings.Logging;


import v9t9.common.client.ISettingsHandler;
import v9t9.common.cpu.ICpu;
import v9t9.common.memory.IMemory;
import v9t9.common.memory.IMemoryEntry;
import v9t9.memory.MultiBankedMemoryEntry;

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