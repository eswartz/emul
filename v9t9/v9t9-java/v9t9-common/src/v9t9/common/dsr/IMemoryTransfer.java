/*
  IMemoryTransfer.java

  (c) 2010-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.dsr;

import v9t9.common.memory.ByteMemoryAccess;

/**
 * This interface encapsulates the kinds of memory access a DSR may perform.
 * It is abstracted out for purposes of unit testing.
 * @author ejs
 *
 */
public interface IMemoryTransfer {

	/**
	 * Read a parameter word
	 */
	short readParamWord(int offset);
	/**
	 * Read a parameter byte
	 */
	byte readParamByte(int offset);
	
	void writeParamByte(int offset, byte val);
	void writeParamWord(int offset, short val);
	/**
	 * Record a write to classic VDP memory
	 * @param vaddr address in 0-0x3FFF range
	 * @param read
	 */
	void dirtyVdpMemory(int vaddr, int read);

	/**
	 * Get memory read/write access to classic VDP memory
	 * @param vaddr address in 0-0x3FFF range
	 * @return access to memory (need {@link #dirtyVdpMemory(short, int)} to notice)
	 */
	ByteMemoryAccess getVdpMemory(int vaddr);
	
	/**
	 * Read byte in classic VDP memory
	 * @param vaddr address in 0-0x3FFF range
	 * @return byte
	 */
	byte readVdpByte(int vaddr);
	
	/**
	 * Read word in classic VDP memory
	 * @param vaddr address in 0-0x3FFF range
	 * @return byte
	 */
	short readVdpShort(int vaddr);
	
	/**
	 * Write byte in classic VDP memory
	 * @param vaddr address in 0-0x3FFF range
	 * @return byte
	 */
	void writeVdpByte(int vaddr, byte byt);

}
