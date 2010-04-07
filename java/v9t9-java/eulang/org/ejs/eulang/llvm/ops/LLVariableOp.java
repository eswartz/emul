/**
 * 
 */
package org.ejs.eulang.llvm.ops;

import org.ejs.eulang.llvm.ILLVariable;

/**
 * @author ejs
 *
 */
public class LLVariableOp implements LLOperand {

	private final ILLVariable var;
	/**
	 * 
	 */
	public LLVariableOp(ILLVariable var) {
		this.var = var;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return var.getSymbol().getLLVMName();
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((var == null) ? 0 : var.hashCode());
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
		LLVariableOp other = (LLVariableOp) obj;
		if (var == null) {
			if (other.var != null)
				return false;
		} else if (!var.equals(other.var))
			return false;
		return true;
	}
	/**
	 * @return
	 */
	public ILLVariable getVariable() {
		return var;
	}

	
	
}
