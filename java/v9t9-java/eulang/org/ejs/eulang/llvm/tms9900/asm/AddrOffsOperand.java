/**
 * 
 */
package org.ejs.eulang.llvm.tms9900.asm;

import org.ejs.eulang.llvm.ops.LLOperand;
import org.ejs.eulang.types.LLType;

import v9t9.tools.asm.assembler.operand.hl.AddrOperand;
import v9t9.tools.asm.assembler.operand.hl.AssemblerOperand;

/**
 * @author ejs
 *
 */
public class AddrOffsOperand extends AddrOperand implements AsmOperand {
	private final AssemblerOperand offset;
	private final LLType type;

	/**
	 * @param llOp
	 * @param local
	 */
	public AddrOffsOperand(LLType type,
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
		return super.toString() + "+" + offset.toString();
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
		AddrOffsOperand other = (AddrOffsOperand) obj;
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

	/**
	 * @return the offset
	 */
	public AssemblerOperand getOffset() {
		return offset;
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
		AssemblerOperand newOffs = offset.replaceOperand(src, dst);
		if (newAddr != getAddr() || newOffs != offset) {
			return new AddrOffsOperand(type, newOffs, newAddr);
		}
		return this;
	}
}
