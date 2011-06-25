/**
 * 
 */
package org.ejs.eulang.llvm.ops;

import java.util.Arrays;

import org.ejs.eulang.types.LLArrayType;
import org.ejs.eulang.types.LLType;

/**
 * @author ejs
 *
 */
public class LLArrayOp extends BaseLLConstOperand {

	private final LLArrayType arrayType;
	private final LLOperand[] elements;

	/**
	 * 
	 */
	public LLArrayOp(LLArrayType type, LLOperand... elements) {
		super(type);
		this.arrayType = type;
		this.elements = elements;
	}

	public LLOperand[] getElements() {
		return elements;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Arrays.hashCode(elements);
		result = prime * result
				+ ((arrayType == null) ? 0 : arrayType.hashCode());
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
		LLArrayOp other = (LLArrayOp) obj;
		if (!Arrays.equals(elements, other.elements))
			return false;
		return true;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[ ");
		boolean first = true;
		LLType type = arrayType.getSubType();
		for (int idx = 0; idx < elements.length; idx++) {
			if (first) first = false; else sb.append(", ");
			sb.append(type.getLLVMName()).append(' ').append(elements[idx]);
		}
		sb.append(" ]");
		return sb.toString();
	}

}
