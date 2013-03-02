/*
  Vdp9938Mmio.java

  (c) 2008-2011 Edward Swartz

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

import v9t9.common.client.ISettingsHandler;
import v9t9.common.memory.IMemory;
import v9t9.engine.video.v9938.VdpV9938;


/** 
 * V9938 mmio chip entry
 * <p>
 * Models the MSX V9938 card access specified in the DIJIT Systems Advanced Video Processor Card (AVPC) manual
 * @author ejs
 */
public class Vdp9938Mmio extends Vdp9918AMmio {

	private BankedMemoryEntry memoryBank;

	private VdpV9938 v9938;

    public Vdp9938Mmio(ISettingsHandler settings, IMemory memory, VdpV9938 vdp, int memSize) {
    	super(settings, memory, vdp, adjustMemorySize(memSize));
    	this.v9938 = vdp;
    }
    
    private static int adjustMemorySize(int memorySize) {
		if (memorySize < 0x4000)
			memorySize = 0x4000;
		else if (memorySize < 0x20000)
			memorySize = 0x20000;
		else
			memorySize = 0x30000;
		return memorySize;
	}

	protected void initMemory(IMemory memory, int memorySize) {
		memoryBank = new WindowBankedMemoryEntry(
				memory, "VDP RAM",
				videoMemory,
				0x0000, 0x4000,
				fullRamArea);
    	this.memoryEntry = memoryBank;
		memory.addAndMap(memoryBank);
    }

	@Override
	protected int getAbsoluteAddress(int vdpaddr) {
		return vdpaddr + (memoryBank.getCurrentBank() << 14);
	}
	
	@Override
	protected void autoIncrementAddr() {
		vdpaddr = vdpaddr+1 & 0x3fff;
		if (vdpaddr == 0 && v9938.isEnhancedMode()) {
			byte vdpbank = (byte) v9938.getRegister(14);
			v9938.setRegister(14, (byte) ((vdpbank + 1) & 0x7));
		}
	}
	
	public void write(int addr, byte val) {
		int port = (addr & 6) >> 1;
    	switch (port) {
    	case 0:
    		writeData(val);
    		break;
    	case 1:
    		writeAddress(val);
    		break;
    	case 2:
			// color data port
    		v9938.writeColorData(val);
			break;
		case 3:
			// indirect data write port
			v9938.writeRegisterIndirect(val);
			break;
    	}
    }

	public BankedMemoryEntry getMemoryBank() {
		return memoryBank;
	}
	
	@Override
	public int getBankAddr() {
		return memoryBank.getCurrentBank() << 14;
	}
	
}
