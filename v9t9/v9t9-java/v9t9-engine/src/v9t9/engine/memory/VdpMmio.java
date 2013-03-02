/*
  VdpMmio.java

  (c) 2008-2012 Edward Swartz

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

import v9t9.common.hardware.IVdpChip;
import v9t9.common.memory.ByteMemoryAccess;

public abstract class VdpMmio implements IConsoleMmioReader, IConsoleMmioWriter {

	protected int currentaccesscycles;
	protected IVdpChip vdpHandler;
	protected VdpRamArea fullRamArea;
	private int fullRamMask;

	public VdpMmio(VdpRamArea fullRamArea) {
		this.fullRamArea = fullRamArea;
		this.fullRamMask = fullRamArea.memory.length - 1;
		fullRamMask |= (fullRamMask >> 1) | (fullRamMask >> 2) | (fullRamMask >> 3);
	}


    public ByteMemoryArea getMemoryArea() {
    	return fullRamArea;
    }
	abstract public int getAddr();

	public int getMemoryAccessCycles() {
		return currentaccesscycles;
	}
	
	/** Set the number of extra access cycles */
	public void setMemoryAccessCycles(int i) {
		currentaccesscycles = i;
	}

	public void setVdpHandler(IVdpChip vdp) {
		this.vdpHandler = vdp;
	}

	public byte readFlatMemory(int vdpaddr) {
		return fullRamArea.memory[vdpaddr & fullRamMask];
	}

	public void writeFlatMemory(int vdpaddr, byte byt) {
		if (vdpaddr >= 0 && vdpaddr < fullRamArea.memory.length)
			fullRamArea.memory[vdpaddr & fullRamMask] = byt;
		vdpHandler.touchAbsoluteVdpMemory(vdpaddr);
	}

	public ByteMemoryAccess getByteReadMemoryAccess(int addr) {
		return new ByteMemoryAccess(fullRamArea.memory, addr);
	}

	public int getMemorySize() {
		return fullRamArea.memory.length;
	}

	/**
	 * Get the base address for the current bank
	 * @return
	 */
	public int getBankAddr() {
		return 0;
	}


	abstract public BankedMemoryEntry getMemoryBank();
}