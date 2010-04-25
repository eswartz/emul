/**
 * 
 */
package org.ejs.eulang.llvm.ops;

import org.ejs.eulang.types.LLType;

/**
 * This is an operand whose type is bitcast.
 * @author ejs
 *
 */
public class LLBitcastOp implements LLOperand {
	private final LLOperand val;
	private final LLType castTo;
	/**
	 * 
	 */
	public LLBitcastOp(LLType castTo, LLOperand val) {
		this.castTo = castTo;
		this.val = val;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.ops.LLOperand#isConstant()
	 */
	@Override
	public boolean isConstant() {
		return val.isConstant();
	}
	
	@Override
	public LLType getType() {
		return castTo;
	}
	@Override
	public String toString() {
		return "bitcast (" + val.getType().getLLVMName() + " " + val.toString() + " to " + castTo.getLLVMName() + ")" ;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((val == null) ? 0 : val.hashCode());
		result = prime * result + ((castTo == null) ? 0 : castTo.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LLBitcastOp other = (LLBitcastOp) obj;
		if (val == null) {
			if (other.val != null)
				return false;
		} else if (!val.equals(other.val))
			return false;
		if (castTo == null) {
			if (other.castTo != null)
				return false;
		} else if (!castTo.equals(other.castTo))
			return false;
		return true;
	}
	
	
}
