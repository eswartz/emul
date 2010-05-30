/**
 * 
 */
package org.ejs.eulang.llvm.tms9900.asm;

import org.ejs.eulang.types.LLType;

import v9t9.tools.asm.assembler.operand.hl.AddrOperand;
import v9t9.tools.asm.assembler.operand.hl.AssemblerOperand;

/**
 * A reference to a variable plus an offset.  Indicates access to a piece
 * of a composite value.  The type is used to distinguish uses (e.g. to
 * avoid aliasing byte vs. word moves).
 * @author ejs
 *
 */
public class CompositePieceOperand extends AddrOperand implements AsmOperand {
	private final AssemblerOperand offset;

	private LLType type;

	/**
	 * @param llOp
	 * @param local
	 */
	public CompositePieceOperand(AssemblerOperand offset,
			AssemblerOperand addr, LLType type) {
		super(addr);
		this.offset = offset;
		this.type = type;
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
		CompositePieceOperand other = (CompositePieceOperand) obj;
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


	public LLType getType() {
		return type;
	}
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
	public AssemblerOperand replaceOperand(AssemblerOperand src,
			AssemblerOperand dst) {
		if (src.equals(this))
			return dst;
		AssemblerOperand newAddr = getAddr().replaceOperand(src, dst);
		AssemblerOperand newOffs = getOffset().replaceOperand(src, dst);
		if (newAddr != getAddr() || newOffs != getOffset()) {
			// swap types (e.g. replace ptr ref with direct value ref): ASSUMED that we intend to do this
			return new CompositePieceOperand(newOffs, newAddr, getType());
		}
		return this;	
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.tms9900.asm.AsmOperand#isConst()
	 */
	@Override
	public boolean isConst() {
		return false;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.operand.hl.AssemblerOperand#addOffset(int)
	 */
	@Override
	public AssemblerOperand addOffset(int i) {
		return new CompositePieceOperand(offset.addOffset(i), getAddr(), null);
	}
}
