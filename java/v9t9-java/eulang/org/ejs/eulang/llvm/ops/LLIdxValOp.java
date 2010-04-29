/**
 * 
 */
package org.ejs.eulang.llvm.ops;

import org.ejs.eulang.types.LLType;

/**
 * @author ejs
 *
 */
public class LLIdxValOp implements LLOperand {
	private final LLOperand val;
	private int idx;
	/**
	 * 
	 */
	public LLIdxValOp(int idx, LLOperand val) {
		this.idx = idx;
		this.val = val;
	}
	@Override
	public LLType getType() {
		return val.getType();
	}
	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.ops.LLOperand#setType(org.ejs.eulang.types.LLType)
	 */
	@Override
	public void setType(LLType type) {
		val.setType(type);
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
		return idx + " " + val;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
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
		LLIdxValOp other = (LLIdxValOp) obj;
		if (val == null) {
			if (other.val != null)
				return false;
		} else if (!val.equals(other.val))
			return false;
		return true;
	}
	
	
}
