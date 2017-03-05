/*
  ConsoleMemoryTransfer.java

  (c) 2010-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
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
