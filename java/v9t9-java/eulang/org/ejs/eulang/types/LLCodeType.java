/**
 * 
 */
package org.ejs.eulang.types;

import java.util.Arrays;

/**
 * @author ejs
 *
 */
public class LLCodeType extends BaseLLType {

	private final LLType retType;
	private final LLType[] argTypes;

	private static int gId;
	
	/**
	 * @param retType 
	 * 
	 */
	public LLCodeType(LLType retType, LLType[] argTypes, int ptrBits) {
		super("__code$"+gId++, ptrBits, toString(retType, argTypes), BasicType.CODE, null);
		this.retType = retType;
		this.argTypes = argTypes;
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



	public static String toString(LLType retType, LLType[] argTypes) {
		StringBuilder sb = new StringBuilder();
		
		boolean first = true;
		for (LLType type : argTypes) {
			if (first)
				first = false;
			else
				sb.append(',');
			if (type != null)
				sb.append(type.toString());
			else
				sb.append("<unknown>");
		}
		sb.append(" => ");
		if (retType != null)
			sb.append(retType.toString());
		else
			sb.append("<unknown>");
		return sb.toString();
	}
	
	public LLType[] getArgTypes() {
		return argTypes;
	}
	
	public LLType getRetType() {
		return retType;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.LLType#isComplete()
	 */
	@Override
	public boolean isComplete() {
		if (retType == null) return false;
		for (LLType arg : argTypes)
			if (arg == null) return false;
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.BaseLLType#isMoreComplete(org.ejs.eulang.types.LLType)
	 */
	@Override
	public boolean isMoreComplete(LLType otherType) {
		if (isComplete())
			return true;
		
		int otherCnt;
		if (otherType instanceof LLCodeType)
			otherCnt = ((LLCodeType) otherType).typeCount();
		else
			return false;
				
		return typeCount() > otherCnt;
	}


	private int typeCount() {
		int cnt = 0;
		if(retType != null && retType.isComplete())
			cnt++;
		for (LLType type : argTypes)
			if (type != null && type.isComplete())
				cnt++;
		return cnt;
	}
}
