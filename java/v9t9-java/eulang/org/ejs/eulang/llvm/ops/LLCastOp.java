/**
 * 
 */
package org.ejs.eulang.llvm.ops;

import org.ejs.eulang.llvm.instrs.ECast;
import org.ejs.eulang.types.LLType;

/**
 * This is an operand whose type is cast to another type in a size-preserving way.
 * @author ejs
 *
 */
public class LLCastOp extends BaseLLOperand {
	private final LLOperand val;
	private final ECast cast;
	public LLCastOp(ECast cast, LLType castTo, LLOperand val) {
		super(castTo);
		this.cast = cast;
		this.val = val;
	}
	
	/**
	 * @return the original value, whose type is original
	 */
	public LLOperand getValue() {
		return val;
	}
	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.ops.LLOperand#isConstant()
	 */
	@Override
	public boolean isConstant() {
		return val.isConstant();
	}
	
	@Override
	public String toString() {
		return cast.getOp() + " (" + val.getType().getLLVMName() + " " + val.toString() + " to " + type.getLLVMName() + ")" ;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((val == null) ? 0 : val.hashCode());
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
		LLCastOp other = (LLCastOp) obj;
		if (val == null) {
			if (other.val != null)
				return false;
		} else if (!val.equals(other.val))
			return false;
		return true;
	}

	/**
	 * @return
	 */
	public ECast getCast() {
		return cast;
	}

	/**
	 * @return
	 */
	public LLType getFromType() {
		return val.getType();
	}
	
	
}
