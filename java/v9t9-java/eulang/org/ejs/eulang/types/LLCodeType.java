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

	/**
	 * @param retType 
	 * 
	 */
	public LLCodeType(LLType retType, LLType[] argTypes, int ptrBits) {
		super(toNameString(retType, argTypes), ptrBits, toString(retType, argTypes), BasicType.CODE, null, argTypes == null);
		this.retType = retType;
		this.argTypes = argTypes != null ? argTypes : NO_TYPES;
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
	 * @param retType
	 * @param argTypes
	 * @return
	 */
	private static String toNameString(LLType retType, LLType[] argTypes) {
		String name = toString(retType, argTypes);
		name = name.replaceAll("\\s+", "");
		name = name.replaceAll("\\(|,", "\\$");
		name = name.replaceAll("[^A-Za-z0_9$]+", "_");
		return name;
	}


	public static String toString(LLType retType, LLType[] argTypes) {
		if (retType == null && argTypes == null)
			return "<code>";

		StringBuilder sb = new StringBuilder();
		if (retType != null)
			sb.append(retType.getLLVMName());
		else
			sb.append("<unknown>");
		
		sb.append(" (");
		boolean first = true;
		for (LLType type : argTypes) {
			if (first)
				first = false;
			else
				sb.append(',');
			if (type != null)
				sb.append(type.getLLVMName());
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
	
	public LLCodeType updateTypes(TypeEngine typeEngine, LLType[] type) {
		LLType retType = type[0];
		LLType[] argTypes = new LLType[type.length - 1];
		System.arraycopy(type, 1, argTypes, 0, argTypes.length);
		return new LLCodeType(retType, argTypes, getBits());
	}
	
}
