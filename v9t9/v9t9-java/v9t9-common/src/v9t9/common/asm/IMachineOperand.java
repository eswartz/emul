/*
  IMachineOperand.java

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
package v9t9.common.asm;

import v9t9.common.cpu.InstructionWorkBlock;

public interface IMachineOperand extends IOperand {

	public static final int OP_NONE = -1;

	boolean isMemory();

	boolean isRegisterReference();

	boolean isRegisterReference(int reg);

	boolean isRegister();

	boolean isRegister(int reg);

	boolean isConstant();

	boolean isLabel();

	/*
	 * Print out an operand into a disassembler operand, returns NULL if no
	 * printable information
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	String toString();

	/**
	 * Advance PC and get cycle count for the size of the operand.
	 * 
	 * @param addr
	 *            is current address
	 * @return new address
	 */
	short advancePc(short addr);


	boolean hasImmediate();

	/**
	 * Get the value of an operand with the given effective address
	 * and current fetched value, for display in a dump or the debugger.
	 * @return
	 */
	String valueString(short ea, short theValue);

	/**
	 * Get the effective address of the operand and fill in its clock cycles.
	 * (Memory cycles are accounted through the memory handler.)
	 * @return
	 */
	short getEA(InstructionWorkBlock block);

	/**
	 * Get the value of the operand with the given effective address
	 * @param memory
	 * @return
	 */
	short getValue(InstructionWorkBlock block, short ea);

	void convertToImmedate();

	/**
	 * @param inst  
	 */
	IOperand resolve(RawInstruction inst);

	int hashCode();

	boolean equals(Object obj);

}