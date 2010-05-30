/**
 * 
 */
package org.ejs.eulang.llvm.tms9900.asm;


import org.ejs.eulang.types.LLType;

import v9t9.tools.asm.assembler.operand.hl.AssemblerOperand;

/**
 * A reference to a memory location and offset (e.g. offset into a stack local)
 * @author ejs
 *
 */
public class StackLocalOffsOperand extends LocalOffsOperand implements AsmOperand {

	private LLType type;

	/**
	 * @param llOp
	 * @param local
	 */
	public StackLocalOffsOperand(AssemblerOperand offset,
			AssemblerOperand addr, LLType type) {
		super(offset, addr);
		this.type = type;
	}
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		StackLocalOffsOperand other = (StackLocalOffsOperand) obj;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}


	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.operand.hl.AddrOperand#toString()
	 */
	@Override
	public String toString() {
		return "@" + getAddr() + "+" +  getOffset().toString() + " [" + type + "]";
	}
	
	/**
	 * @param type the type to set
	 */
	public void setType(LLType type) {
		this.type = type;
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
				return new StackLocalOffsOperand(newOffs, newAddr, type);
		}
		return this;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.operand.hl.AddrOperand#addOffset(int)
	 */
	@Override
	public AssemblerOperand addOffset(int i) {
		return new StackLocalOffsOperand(getOffset().addOffset(i), getAddr(), null);
	}

}
