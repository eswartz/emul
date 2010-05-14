/**
 * 
 */
package org.ejs.eulang.llvm.instrs;

import org.ejs.eulang.llvm.ops.LLOperand;
import org.ejs.eulang.types.LLType;

/**
 * @author ejs
 *
 */
public class LLCompareInstr extends LLBinaryInstr {
	
	private final String cmp;
	/**
	 * Create with ops= {ret, op1, op2}; 
	 */
	public LLCompareInstr(String opName, String cmp, LLOperand ret, LLType type, LLOperand... ops) {
		super(opName, ret, ret.getType(), ops);
		this.cmp = cmp;
		if (ops.length != 2)
			throw new IllegalArgumentException();
	}
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((cmp == null) ? 0 : cmp.hashCode());
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
		LLCompareInstr other = (LLCompareInstr) obj;
		if (cmp == null) {
			if (other.cmp != null)
				return false;
		} else if (!cmp.equals(other.cmp))
			return false;
		return true;
	}


	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.instrs.LLTypedInstr#appendOptionString(java.lang.StringBuilder)
	 */
	@Override
	protected void appendOptionString(StringBuilder sb) {
		sb.append(cmp).append(' ');
		sb.append(ops[0].getType().getLLVMName()).append(' ');
	}
	
	/**
	 * @return the cmp
	 */
	public String getCmp() {
		return cmp;
	}
	
	/**  Return value:  llGetOperands()[0] */
	public LLOperand ret() { return (LLOperand) ops[0]; }
	/**  llGetOperands()[1] */
	public LLOperand op1() { return ops[1]; }
	/**  llGetOperands()[2] */
	public LLOperand op2() { return ops[2]; }
	
	
}
