/**
 * 
 */
package org.ejs.eulang.llvm.ops;

import org.ejs.eulang.types.LLType;

/**
 * @author ejs
 *
 */
public class LLTypeIdxOp implements LLOperand {
	private final LLType type;
	private final int idx;
	/**
	 * 
	 */
	public LLTypeIdxOp(LLType type, int idx) {
		this.type = type;
		this.idx = idx;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return type.toString() + " " + idx;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + idx;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		LLTypeIdxOp other = (LLTypeIdxOp) obj;
		if (idx != other.idx)
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}
	
	
}
