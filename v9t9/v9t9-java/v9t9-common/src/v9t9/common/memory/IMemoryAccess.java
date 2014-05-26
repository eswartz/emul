/*
  IMemoryAccess.java

  (c) 2008-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
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
	 * Read a word at the given address, without side effects.
	 * @param addr address
	 */
	short flatReadWord(int addr);

	/**
	 * Read a byte at the given address, without side effects.
	 * @param addr address
	 */
    byte flatReadByte(int addr);

	/**
	 * Write a word at the given address, without side effects.
	 * @param addr address
	 */
    void flatWriteWord(int addr, short val);

	/**
	 * Write a byte at the given address, without side effects.
	 * @param addr address
	 */
    void flatWriteByte(int addr, byte val);

	/**
	 * Read a word at the given address.
	 * @param addr address
	 */
    short readWord(int addr);

	/**
	 * Read a byte at the given address.
	 * @param addr address
	 */
    byte readByte(int addr);

	/**
	 * Write a word at the given address.
	 * @param addr address
	 */
    void writeWord(int addr, short val);

	/**
	 * Write a byte at the given address.
	 * @param addr address
	 */
    void writeByte(int addr, byte val);

}
