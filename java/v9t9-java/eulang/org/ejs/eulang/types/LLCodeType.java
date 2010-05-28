/**
 * 
 */
package org.ejs.eulang.types;

import java.util.Arrays;

import org.ejs.eulang.TypeEngine;

/**
 * @author ejs
 *
 */
public class LLCodeType extends BaseLLAggregateType  {

	private final LLType retType;
	private final LLType[] argTypes;
	private final LLType[] types;
	private boolean isEmpty;
	
	/**
	 * @param retType 
	 * 
	 */
	public LLCodeType(LLType retType, LLType[] argTypes, int ptrBits) {
		super(fixLLVMName(toString(retType, argTypes, true)), ptrBits, toString(retType, argTypes, false), BasicType.CODE, null, argTypes == null);
		this.retType = retType;
		this.argTypes = argTypes != null ? argTypes : NO_TYPES;
		this.isEmpty = argTypes == null;
		this.types = new LLType[1 + this.argTypes.length];
		types[0] = retType;
		if (argTypes != null)
			System.arraycopy(argTypes, 0, types, 1, argTypes.length);
	}
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Arrays.hashCode(argTypes);
		result = prime * result + ((retType == null) ? 0 : retType.hashCode());
		result = prime * result + (isEmpty ? 1234 : 0);
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
		LLCodeType other = (LLCodeType) obj;
		if (isEmpty != other.isEmpty)
			return false;
		if (!Arrays.equals(argTypes, other.argTypes))
			return false;
		if (retType == null) {
			if (other.retType != null)
				return false;
		} else if (!retType.equals(other.retType))
			return false;
		return true;
	}


	/**
	 * @return the isEmpty
	 */
	public boolean isEmpty() {
		return isEmpty;
	}

	public static String toString(LLType retType, LLType[] argTypes, boolean allowIncomplete) {
		if (retType == null && argTypes == null)
			return allowIncomplete ? "<code>" : null;

		StringBuilder sb = new StringBuilder();
		if (retType != null && retType.isComplete())
			sb.append(retType.getLLVMName());
		else if (allowIncomplete)
			sb.append("<unknown>");
		else
			return null;
		
		sb.append(" (");
		boolean first = true;
		for (LLType type : argTypes) {
			if (first)
				first = false;
			else
				sb.append(',');
			if (!allowIncomplete && (type == null || !type.isComplete()))
				return null;
			if (type != null)
				if (type.isComplete())
					sb.append(type.getLLVMName());
				else
					sb.append(type);
			else
				sb.append("<unknown>");
		}
		sb.append(')');
		
		return sb.toString();
	}
	
	public LLType[] getArgTypes() {
		return argTypes;
	}
	
	public LLType getRetType() {
		return retType;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.LLAggregateType#getCount()
	 */
	@Override
	public int getCount() {
		return 1 + argTypes.length;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.LLAggregateType#getTypes()
	 */
	@Override
	public LLType[] getTypes() {
		return types;
	}
	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.LLAggregateType#getType(int)
	 */
	@Override
	public LLType getType(int idx) {
		if (idx == 0)
			return retType;
		return argTypes[idx - 1];
	}
	
	public LLType updateTypes(TypeEngine typeEngine, LLType[] type) {
		LLType retType = type[0];
		LLType[] argTypes = new LLType[type.length - 1];
		System.arraycopy(type, 1, argTypes, 0, argTypes.length);
		return typeEngine.getCodeType(retType, argTypes);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.BaseLLAggregateType#isMoreComplete(org.ejs.eulang.types.LLType)
	 */
	@Override
	public boolean isMoreComplete(LLType otherType) {
		if (otherType instanceof LLCodeType) {
			if (isEmpty && !((LLCodeType) otherType).isEmpty)
				return false;
			else if (!isEmpty && ((LLCodeType) otherType).isEmpty)
				return true;
		}
		return super.isMoreComplete(otherType);
	}
}
