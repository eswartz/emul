/**
 * 
 */
package org.ejs.eulang.llvm.tms9900.asm;

import org.ejs.eulang.types.LLType;

import v9t9.tools.asm.assembler.operand.hl.AssemblerOperand;

/**
 * A reference to memory offset from a register
 * @author ejs
 *
 */
public class RegTempOffsOperand extends LocalOffsOperand implements AsmOperand {

	/**
	 * @param llOp
	 * @param local
	 */
	public RegTempOffsOperand(LLType type,
			AssemblerOperand offset,
			AssemblerOperand addr) {
		super(type, offset, addr);
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.operand.hl.BaseOperand#replaceOperand(v9t9.tools.asm.assembler.operand.hl.AssemblerOperand, v9t9.tools.asm.assembler.operand.hl.AssemblerOperand)
	 */
	@Override
	public AssemblerOperand replaceOperand(AssemblerOperand src,
			AssemblerOperand dst) {
		if (src.equals(this))
			return dst;
		AssemblerOperand newAddr = getAddr().replaceOperand(src, dst);
		AssemblerOperand newOffs = getOffset().replaceOperand(src, dst);
		if (newAddr != getAddr() || newOffs != getOffset()) {
			// swap types (e.g. replace ptr ref with direct value ref): ASSUMED that we intend to do this
			if (newAddr.isMemory())
				return new StackLocalOffsOperand(getType(), newOffs, newAddr);
			else
				return new RegTempOffsOperand(getType(), newOffs, newAddr);
		}
		return this;
	}
	
}
