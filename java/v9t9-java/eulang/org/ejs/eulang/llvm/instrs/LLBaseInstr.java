/**
 * 
 */
package org.ejs.eulang.llvm.instrs;

import java.util.Arrays;

import org.ejs.eulang.llvm.ops.LLOperand;

/**
 * @author ejs
 *
 */
public abstract class LLBaseInstr implements LLInstr {

	protected final String name;
	protected final LLOperand[] ops;

	/**
	 * 
	 */
	public LLBaseInstr(String name, LLOperand... ops) {
		this.name = name;
		this.ops = ops;
	}
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + Arrays.hashCode(ops);
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
		LLBaseInstr other = (LLBaseInstr) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (!Arrays.equals(ops, other.ops))
			return false;
		return true;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		appendInstrString(sb);
		return sb.toString();
	}

	protected void appendInstrString(StringBuilder sb) {
		sb.append(name); sb.append(' ');
		appendOptionString(sb);
		boolean first = true;
		for (LLOperand op : ops) {
			if (first)
				first = false;
			else
				sb.append(", ");
			sb.append(op.toString());
		}
	}
	
	/**
	 * Override if the #toString() method has options before operands
	 */
	protected void appendOptionString(StringBuilder sb) {
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.LLInstruction#getName()
	 */
	@Override
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.LLInstruction#getOperands()
	 */
	@Override
	public LLOperand[] getOperands() {
		return ops;
	}

}
