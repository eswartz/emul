/*
  IMemoryTransfer.java

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
