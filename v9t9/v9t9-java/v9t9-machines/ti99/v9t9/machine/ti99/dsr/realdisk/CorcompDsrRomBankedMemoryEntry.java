/*
  PCodeDsrRomBankedMemoryEntry.java

  (c) 2010-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.machine.ti99.dsr.realdisk;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.memory.IMemory;
import v9t9.common.memory.IMemoryArea;
import v9t9.common.memory.IMemoryEntry;
import v9t9.engine.memory.MultiBankedMemoryEntry;
import v9t9.engine.memory.WordMemoryArea;

/**
 * @author ejs
 *
 */
public class CorcompDsrRomBankedMemoryEntry extends MultiBankedMemoryEntry {
	public static final int RAM_END = 0x4080;
	private WordMemoryArea ramRomArea;

	public CorcompDsrRomBankedMemoryEntry() {
	}
	public CorcompDsrRomBankedMemoryEntry(ISettingsHandler settings, IMemory memory, String name,
			IMemoryEntry[] banks) {
		super(settings, memory, name, banks);
		IMemoryArea romArea = banks[1].getArea();
		ramRomArea = new WordMemoryArea(romArea.getLatency());
		ramRomArea.write = ramRomArea.read = ramRomArea.memory = new short[(RAM_END - 0x4000) / 2];
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.memory.MemoryEntry#readByte(int)
	 */
	@Override
	public byte readByte(int addr) {
		if (addr < RAM_END && getCurrentBank() == 1)
			return ramRomArea.readByte(this, addr);
		return super.readByte(addr);
	}
	/* (non-Javadoc)
	 * @see v9t9.engine.memory.MemoryEntry#readWord(int)
	 */
	@Override
	public short readWord(int addr) {
		if (addr < RAM_END && getCurrentBank() == 1)
			return ramRomArea.readWord(this, addr);
		return super.readWord(addr);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.memory.MemoryEntry#writeWord(int, short)
	 */
	@Override
	public void writeWord(int addr, short val) {
		if (addr < RAM_END && getCurrentBank() == 1)
			ramRomArea.writeWord(this, addr, val);
	}
	@Override
	public void writeByte(int addr, byte val) {
		if (addr < RAM_END && getCurrentBank() == 1)
			ramRomArea.writeByte(this, addr, val);
	}
}
