/**
 * 
 */
package org.ejs.eulang.llvm.ops;

/**
 * @author ejs
 *
 */
public class LLTempOp implements LLOperand {
	private int id;
	/**
	 * 
	 */
	public LLTempOp(int id) {
		this.id = id;
	}
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
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
		LLTempOp other = (LLTempOp) obj;
		if (id != other.id)
			return false;
		return true;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "%" + id;
	}
}
