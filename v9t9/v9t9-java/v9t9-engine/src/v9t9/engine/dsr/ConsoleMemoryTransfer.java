/*
  ConsoleMemoryTransfer.java

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
package v9t9.engine.dsr;

import v9t9.common.dsr.IMemoryTransfer;
import v9t9.common.hardware.IVdpChip;
import v9t9.common.memory.ByteMemoryAccess;
import v9t9.common.memory.IMemoryDomain;
import v9t9.engine.memory.VdpMmio;

/**
 * @author ejs
 *
 */
public class ConsoleMemoryTransfer implements IMemoryTransfer {

	private final IVdpChip vdpHandler;
	private final IMemoryDomain console;
	private final short rambase;
	private final VdpMmio vdpMmio;

	public ConsoleMemoryTransfer(IMemoryDomain console, IVdpChip vdpHandler, VdpMmio vdpMmio, short rambase) {
		this.console = console;
		this.vdpHandler = vdpHandler;
		this.vdpMmio = vdpMmio;
		this.rambase = rambase;
		
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.dsrs.MemoryTransfer#readParamWord(int)
	 */
	public short readParamWord(int offset) {
		return console.readWord(rambase + offset);
	}
	
	public byte readParamByte(int offset) {
		return console.readByte(rambase + offset);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.dsrs.MemoryTransfer#writeParamByte(int, byte)
	 */
	public void writeParamByte(int offset, byte val) {
		console.writeByte(rambase + offset, val);
	}
	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.dsrs.MemoryTransfer#writeParamWord(int, short)
	 */
	public void writeParamWord(int offset, short val) {
		console.writeWord(rambase + offset, val);
		
	}
	/**
	 * Record a write to classic VDP memory
	 * @param vaddr address in 0-0x3FFF range
	 * @param read
	 */
	public void dirtyVdpMemory(int vaddr, int read) {
		int base = vdpMmio.getBankAddr();
		while (read-- > 0) {
			vdpHandler.touchAbsoluteVdpMemory(
					base + vaddr);
			vaddr++;
		}
	}

	/**
	 * Get memory read/write access to classic VDP memory
	 * @param vaddr address in 0-0x3FFF range
	 * @return access to memory (need {@link #dirtyVdpMemory(short, int)} to notice)
	 */
	public ByteMemoryAccess getVdpMemory(int vaddr) {
		return vdpHandler.getByteReadMemoryAccess(
				vdpMmio.getBankAddr() + vaddr);
	}
	
	/**
	 * Read byte in classic VDP memory
	 * @param vaddr address in 0-0x3FFF range
	 * @return byte
	 */
	public byte readVdpByte(int vaddr) {
		int base = vdpMmio.getBankAddr();
		return vdpHandler.readAbsoluteVdpMemory(base + vaddr);
	}
	
	/**
	 * Read word in classic VDP memory
	 * @param vaddr address in 0-0x3FFF range
	 * @return byte
	 */
	public short readVdpShort(int vaddr) {
		int base = vdpMmio.getBankAddr();
		return (short) ((vdpHandler.readAbsoluteVdpMemory(base + vaddr) << 8)
			| (vdpHandler.readAbsoluteVdpMemory(base + vaddr + 1) & 0xff));
	}
	
	/**
	 * Write byte in classic VDP memory
	 * @param vaddr address in 0-0x3FFF range
	 * @return byte
	 */
	public void writeVdpByte(int vaddr, byte byt) {
		int base = vdpMmio.getBankAddr();
		vdpHandler.writeAbsoluteVdpMemory(base + vaddr, byt);
	}

}
