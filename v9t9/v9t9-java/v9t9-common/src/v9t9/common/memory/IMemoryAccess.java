/*
  IMemoryAccess.java

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
package v9t9.common.memory;

/**
 * This interface is the primary way that clients get access to memory.
 * 
 * @author ejs
 *
 */
public interface IMemoryAccess {
	/**
	 * Read a word at the given 16-bit address, without side effects.
	 * @param addr address
	 */
	short flatReadWord(int addr);

	/**
	 * Read a byte at the given 16-bit address, without side effects.
	 * @param addr address
	 */
    byte flatReadByte(int addr);

	/**
	 * Write a word at the given 16-bit address, without side effects.
	 * @param addr address
	 */
    void flatWriteWord(int addr, short val);

	/**
	 * Write a byte at the given 16-bit address, without side effects.
	 * @param addr address
	 */
    void flatWriteByte(int addr, byte val);

	/**
	 * Read a word at the given 16-bit address.
	 * @param addr address
	 */
    short readWord(int addr);

	/**
	 * Read a byte at the given 16-bit address.
	 * @param addr address
	 */
    byte readByte(int addr);

	/**
	 * Write a word at the given 16-bit address.
	 * @param addr address
	 */
    void writeWord(int addr, short val);

	/**
	 * Write a byte at the given 16-bit address.
	 * @param addr address
	 */
    void writeByte(int addr, byte val);

}
