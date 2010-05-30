/**
 * 
 */
package org.ejs.eulang.llvm.tms9900.asm;


import v9t9.tools.asm.assembler.operand.hl.AssemblerOperand;
import v9t9.tools.asm.assembler.operand.hl.NumberOperand;

/**
 * A reference to a memory location and offset (e.g. offset into a stack local)
 * @author ejs
 *
 */
public class StackLocalOffsOperand extends LocalOffsOperand implements AsmOperand {

	/**
	 * @param llOp
	 * @param local
	 */
	public StackLocalOffsOperand(AssemblerOperand offset,
			AssemblerOperand addr) {
		super(offset, addr);
	}
	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.operand.hl.AddrOperand#toString()
	 */
	@Override
	public String toString() {
		if (getOffset() == null || (getOffset() instanceof NumberOperand && ((NumberOperand) getOffset()).getValue() == 0))
			return "@" + getAddr();
		return "@" + getAddr() + "+" +  getOffset().toString();
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
			// swap types (e.g. replace stack ref with something pointed to by reg): ASSUMED that we intend to do this
			if (newAddr.isRegister())
				return new RegTempOffsOperand(newOffs, newAddr);
			else
				return new StackLocalOffsOperand(newOffs, newAddr);
		}
		return this;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.operand.hl.AddrOperand#addOffset(int)
	 */
	@Override
	public AssemblerOperand addOffset(int i) {
		return new StackLocalOffsOperand(getOffset().addOffset(i), getAddr());
	}

}
