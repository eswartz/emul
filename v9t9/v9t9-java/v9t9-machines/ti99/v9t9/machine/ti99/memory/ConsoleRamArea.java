/*
  ConsoleRamArea.java

  (c) 2008-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.machine.ti99.memory;

import ejs.base.properties.IProperty;
import v9t9.common.client.ISettingsHandler;
import v9t9.common.settings.SettingSchema;

/** Builtin console RAM: 256 bytes */
public class ConsoleRamArea extends ConsoleMemoryArea {
	static public final SettingSchema settingEnhRam = new SettingSchema(
			ISettingsHandler.MACHINE, "ExtraConsoleRAM", Boolean.FALSE);
	private final IProperty enhRam;

	public ConsoleRamArea(ISettingsHandler settings) {
		super(0);
		this.enhRam = settings.get(ConsoleRamArea.settingEnhRam);

		memory = new short[0x400 / 2];
		read = memory;
		write = memory;
	}

	private int maskAddress(int addr) {
		if (!enhRam.getBoolean())
			addr = (addr & 0xff) + 0x8300;
		return addr;
	}

	@Override
	public byte readByte(int addr) {
		return super.readByte(maskAddress(addr));
	}

	@Override
	public short readWord(int addr) {
		return super.readWord(maskAddress(addr));
	}

	@Override
	public void writeByte(int addr, byte val) {
		super.writeByte(maskAddress(addr), val);
	}

	@Override
	public void writeWord(int addr, short val) {
		super.writeWord(maskAddress(addr), val);
	}

}