/**
 * 
 */
package org.ejs.eulang.llvm.instrs;

import java.util.Arrays;

import org.ejs.eulang.llvm.ILLCodeVisitor;
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
		for (LLOperand op : ops)
			if (op == null)
				throw new IllegalArgumentException();
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
		sb.append('\t');
		sb.append(name); sb.append(' ');
		appendOptionString(sb);
		boolean first = noCommaBeforeOperands();
		int idx = 0;
		for (LLOperand op : ops) {
			if (first)
				first = false;
			else
				sb.append(", ");
			appendOperandString(sb, idx, op);
			idx++;
		}
	}
	
	/**
	 * @return
	 */
	protected boolean noCommaBeforeOperands() {
		return true;
	}


	/**
	 * Override if the #toString() method has options before operands
	 */
	protected void appendOptionString(StringBuilder sb) {
	}
	
	/**
	 * Override if the operand needs special tuning
	 */
	protected void appendOperandString(StringBuilder sb, int idx, LLOperand op) {
		sb.append(op.toString());
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

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.instrs.LLInstr#accept(org.ejs.eulang.llvm.ILLCodeVisitor)
	 */
	@Override
	public void accept(ILLCodeVisitor visitor) {
		try {
			if (!visitor.enterInstr(this)) {
				for (LLOperand op : ops) {
					op.accept(visitor);
				}
			}
			visitor.exitInstr(this);
		} catch (ILLCodeVisitor.Terminate e) {
			
		}
	}
}
