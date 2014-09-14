/*
  IMachineOperand.java

  (c) 2008-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.asm;

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
//	@Deprecated
//	short getEA(InstructionWorkBlock block);

	/**
	 * Get the value of the operand with the given effective address
	 * @param memory
	 * @return
	 */
//	@Deprecated
//	short getValue(InstructionWorkBlock block, short ea);

	void convertToImmedate();

	/**
	 * @param inst  
	 */
	IOperand resolve(RawInstruction inst);

	int hashCode();

	boolean equals(Object obj);

}