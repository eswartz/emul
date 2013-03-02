/*
  PCodeDsrRomBankedMemoryEntry.java

  (c) 2010-2011 Edward Swartz

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
package v9t9.machine.ti99.dsr.pcode;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.memory.IMemory;
import v9t9.common.memory.IMemoryEntry;
import v9t9.engine.hardware.ICruWriter;
import v9t9.engine.memory.GplMmio;
import v9t9.engine.memory.MultiBankedMemoryEntry;
import v9t9.machine.ti99.machine.TI99Machine;

/**
 * @author ejs
 *
 */
public class PCodeDsrRomBankedMemoryEntry extends MultiBankedMemoryEntry {

	private GplMmio pcodeGromMmio;

	public PCodeDsrRomBankedMemoryEntry() {
	}
	public PCodeDsrRomBankedMemoryEntry(ISettingsHandler settings, IMemory memory, String name,
			IMemoryEntry[] banks) {
		super(settings, memory, name, banks);
		

	}
	
	public void setup(TI99Machine machine, GplMmio pcodeGromMmio) {
		this.pcodeGromMmio = pcodeGromMmio;
		
		// bit 0 (0x1f00) handled as DSR
		machine.getCruManager().removeWriter(0x1f80, 1);
		machine.getCruManager().removeWriter(0x1f86, 1);
		
		machine.getCruManager().add(0x1f80, 1, 
				new ICruWriter() {

					@Override
					public int write(int addr, int data, int num) {
						selectBank(data & 1);
						return 0;
					}
			
		});
		
		machine.getCruManager().add(0x1f86, 1, 
				new ICruWriter() {

					@Override
					public int write(int addr, int data, int num) {
						// blink light
						System.out.println("*** PCODE blink " + (data == 0 ? "off" : "on"));
						return 0;
					}
			
		});
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.memory.MemoryEntry#readByte(int)
	 */
	@Override
	public byte readByte(int addr) {
		if (addr >= 0x5bfc && addr <= 0x5bff) {
			return pcodeGromMmio.read(addr);
		}
		return super.readByte(addr);
	}

	@Override
	public void writeByte(int addr, byte val) {
		if (addr >= 0x5ffc && addr <= 0x5fff) {
			pcodeGromMmio.write(addr, val);
			return;
		}
		super.writeByte(addr, val);
	}

}
