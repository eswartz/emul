/**
 * 
 */
package org.ejs.eulang.llvm.tms9900.asm;

import org.ejs.eulang.types.LLType;

import v9t9.tools.asm.assembler.operand.hl.AddrOperand;
import v9t9.tools.asm.assembler.operand.hl.AssemblerOperand;

/**
 * A reference to a local plus an offset -- may refer to the address or the
 * value of the local, depending on subclass.
 * @author ejs
 *
 */
public abstract class LocalOffsOperand extends AddrOperand implements AsmOperand {
	private final AssemblerOperand offset;
	private LLType type;

	/**
	 * @param llOp
	 * @param local
	 */
	public LocalOffsOperand(LLType type,
			AssemblerOperand offset,
			AssemblerOperand addr) {
		super(addr);
		this.type = type;
		this.offset = offset;
	}
	

	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.operand.hl.AddrOperand#toString()
	 */
	@Override
	public String toString() {
		return "@" + offset.toString() + "(" + getAddr() + ")";
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((offset == null) ? 0 : offset.hashCode());
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
		LocalOffsOperand other = (LocalOffsOperand) obj;
		if (offset == null) {
			if (other.offset != null)
				return false;
		} else if (!offset.equals(other.offset))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.tms9900.asm.AsmOperand#getType()
	 */
	@Override
	public LLType getType() {
		return type;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.tms9900.asm.AsmOperand#setType(org.ejs.eulang.types.LLType)
	 */
	@Override
	public void setType(LLType type) {
		this.type = type;
	}
	/**
	 * @return the offset
	 */
	public AssemblerOperand getOffset() {
		return offset;
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.operand.hl.BaseOperand#replaceOperand(v9t9.tools.asm.assembler.operand.hl.AssemblerOperand, v9t9.tools.asm.assembler.operand.hl.AssemblerOperand)
	 */
	abstract public AssemblerOperand replaceOperand(AssemblerOperand src,
			AssemblerOperand dst);
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.tms9900.asm.AsmOperand#isConst()
	 */
	@Override
	public boolean isConst() {
		return false;
	}
}
