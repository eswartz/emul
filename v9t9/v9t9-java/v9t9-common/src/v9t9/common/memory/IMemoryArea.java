/*
  IMemoryArea.java

  (c) 2011 Edward Swartz

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
package v9t9.common.memory;


/**
 * @author ejs
 *
 */
public interface IMemoryArea extends Comparable<IMemoryArea> {

	boolean hasWriteAccess();

	boolean hasReadAccess();

	/**
	 * Read a word at the given 16-bit address, without side effects.
	 * @param entry
	 * @param addr address
	 */
	short flatReadWord(IMemoryEntry entry, int addr);

	/**
	 * Read a byte at the given 16-bit address, without side effects.
	 * @param entry
	 * @param addr address
	 */
	byte flatReadByte(IMemoryEntry entry, int addr);

	/**
	 * Write a word at the given 16-bit address, without side effects.
	 * @param entry
	 * @param addr address
	 */
	void flatWriteWord(IMemoryEntry entry, int addr, short val);

	/**
	 * Write a byte at the given 16-bit address, without side effects.
	 * @param entry
	 * @param addr address
	 */
	void flatWriteByte(IMemoryEntry entry, int addr, byte val);

	/**
	 * Save the content of the memory the given array (sized based on the entry)
	 * @param array
	 */
	void copyToBytes(byte[] array);

	/**
	 * Read the content of the memory the given array (sized based on the entry)
	 * @param array
	 */
	void copyFromBytes(byte[] array);

	void setLatency(int latency);

}