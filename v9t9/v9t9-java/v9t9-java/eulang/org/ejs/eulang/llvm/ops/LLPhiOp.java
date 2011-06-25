/**
 * 
 */
package org.ejs.eulang.llvm.ops;

/**
 * @author ejs
 *
 */
public class LLPhiOp extends BaseLLOperand {
	private LLSymbolOp label;
	private LLOperand value;
	public LLPhiOp(LLOperand value, LLSymbolOp label) {
		super(value.getType());
		this.value = value;
		this.label = label;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "[ " + value.toString() + ", " + label.toString() + "]";
	}

	
	public LLSymbolOp getLabel() {
		return label;
	}

	public void setLabel(LLSymbolOp label) {
		this.label = label;
	}

	public LLOperand getValue() {
		return value;
	}

	public void setValue(LLOperand value) {
		this.value = value;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.ops.BaseLLOperand#isConstant()
	 */
	@Override
	public boolean isConstant() {
		return value.isConstant();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		LLPhiOp other = (LLPhiOp) obj;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
	
	
}
