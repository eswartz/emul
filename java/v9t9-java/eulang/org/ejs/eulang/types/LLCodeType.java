/**
 * 
 */
package org.ejs.eulang.types;

import java.util.Arrays;

/**
 * @author ejs
 *
 */
public class LLCodeType implements LLType {

	private final int ptrBits;
	private final LLType retType;
	private final LLType[] argTypes;

	/**
	 * @param retType 
	 * 
	 */
	public LLCodeType(LLType retType, LLType[] argTypes, int ptrBits) {
		this.retType = retType;
		this.argTypes = argTypes;
		this.ptrBits = ptrBits;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.LLType#getBasicType()
	 */
	@Override
	public BasicType getBasicType() {
		return BasicType.CODE;
	}	

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (retType != null)
			sb.append(retType.toString());
		else
			sb.append("<unknown>");
		boolean first = true;
		for (LLType type : argTypes) {
			if (first) {
				sb.append('='); first = false;
			} else
				sb.append(',');
			if (type != null)
				sb.append(type.toString());
			else
				sb.append("<unknown>");
		}
		return sb.toString();
	}
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(argTypes);
		result = prime * result + ptrBits;
		result = prime * result + ((retType == null) ? 0 : retType.hashCode());
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
		LLCodeType other = (LLCodeType) obj;
		if (!Arrays.equals(argTypes, other.argTypes))
			return false;
		if (ptrBits != other.ptrBits)
			return false;
		if (retType == null) {
			if (other.retType != null)
				return false;
		} else if (!retType.equals(other.retType))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.LLType#getBits()
	 */
	@Override
	public int getBits() {
		return ptrBits;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.LLType#getSubType()
	 */
	@Override
	public LLType getSubType() {
		return null;
	}

	/**
	 */
	public LLType[] getArgTypes() {
		return argTypes;
	}
	
	/**
	 * @return the retType
	 */
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
}
