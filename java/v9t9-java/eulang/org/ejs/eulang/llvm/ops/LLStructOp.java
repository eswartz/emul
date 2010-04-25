/**
 * 
 */
package org.ejs.eulang.llvm.ops;

import java.util.Arrays;

import org.ejs.eulang.types.LLAggregateType;

/**
 * @author ejs
 *
 */
public class LLStructOp extends BaseLLConstOperand {

	private final LLAggregateType tupleType;
	private final LLOperand[] elements;

	/**
	 * 
	 */
	public LLStructOp(LLAggregateType type, LLOperand[] elements) {
		super(type);
		this.tupleType = type;
		this.elements = elements;
	}

	public LLAggregateType getTupleType() {
		return tupleType;
	}
	
	public LLOperand[] getElements() {
		return elements;
	}
	
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(elements);
		result = prime * result
				+ ((tupleType == null) ? 0 : tupleType.hashCode());
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
		LLStructOp other = (LLStructOp) obj;
		if (!Arrays.equals(elements, other.elements))
			return false;
		if (tupleType == null) {
			if (other.tupleType != null)
				return false;
		} else if (!tupleType.equals(other.tupleType))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{ ");
		boolean first = true;
		for (int idx = 0; idx < elements.length; idx++) {
			if (first) first = false; else sb.append(", ");
			sb.append(elements[idx].getType().getLLVMName()).append(' ').append(elements[idx]);
		}
		sb.append(" }");
		return sb.toString();
	}

}
