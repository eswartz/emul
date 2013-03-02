/*
  ConsoleRamArea.java

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
package v9t9.machine.ti99.memory;


import ejs.base.properties.IProperty;
import v9t9.common.client.ISettingsHandler;
import v9t9.common.memory.IMemoryEntry;
import v9t9.common.settings.SettingSchema;

/** Builtin console RAM: 256 bytes */
public class ConsoleRamArea extends ConsoleMemoryArea {
    static public final SettingSchema settingEnhRam = new SettingSchema(
    		ISettingsHandler.MACHINE,
    		"ExtraConsoleRAM", Boolean.FALSE);
	private final IProperty enhRam;

	public ConsoleRamArea(ISettingsHandler settings) {
    	super(0);
		this.enhRam = settings.get(ConsoleRamArea.settingEnhRam);
    	
        memory = new short[0x400/2];
        read = memory;
        write = memory;
    }
	
	private int maskAddress(int addr) {
		if (!enhRam.getBoolean())
			 addr = (addr & 0xff) + 0x8300;
		return addr;
	}

	public byte readByte(IMemoryEntry entry, int addr) {
		 return super.readByte(entry, maskAddress(addr));
     }

	public short readWord(IMemoryEntry entry, int addr) {
		 return super.readWord(entry, maskAddress(addr));
     }
     public void writeByte(IMemoryEntry entry, int addr, byte val) {
		 super.writeByte(entry, maskAddress(addr), val);
     }
     public void writeWord(IMemoryEntry entry, int addr, short val) {
		 super.writeWord(entry, maskAddress(addr), val);
     }
     
}