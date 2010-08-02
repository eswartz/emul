package v9t9.engine.cpu;

import v9t9.engine.memory.MemoryDomain;
import v9t9.tools.asm.assembler.Assembler;
import v9t9.tools.asm.assembler.ResolveException;

public interface MachineOperand extends Operand {

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

	/**
	 * Read any extra immediates for an operand from the instruction stream.
	 * Fills in Operand.size and Operand.immed.
	 * 
	 * @param w
	 * @param addr
	 *            is current address
	 * @param pc
	 *            address of instruction
	 * @param wp
	 *            workspace pointer
	 * @return new address
	 */
	short fetchOperandImmediates(MemoryDomain domain, short addr);

	boolean hasImmediate();

	/**
	 * @return
	 */
	String valueString(short ea, short theValue);

	/**
	 * Get the effective address of the operand and fill in its clock cycles.
	 * (Memory cycles are accounted through the memory handler.)
	 * @return
	 */
	short getEA(MemoryDomain domain, int pc, short wp);

	/**
	 * @param memory
	 * @return
	 */
	short getValue(MemoryDomain domain, short ea);

	void convertToImmedate();

	/** Generate the bits for the operand, or throw IllegalArgumentException
	 * for a non-machine operand */
	int getBits();

	/**
	 * @param inst  
	 */
	Operand resolve(RawInstruction inst);

	int hashCode();

	boolean equals(Object obj);

	/**
	 * @param assembler  
	 */
	MachineOperand resolve(Assembler assembler, IInstruction inst)
			throws ResolveException;

}