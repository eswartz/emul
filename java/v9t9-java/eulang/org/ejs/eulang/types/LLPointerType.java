/**
 * 
 */
package org.ejs.eulang.types;

/**
 * @author ejs
 *
 */
public class LLPointerType implements LLType {

	private final LLType baseType;
	private final int bits;

	public LLPointerType(int bits, LLType baseType) {
		this.bits = bits;
		this.baseType = baseType;
		
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return baseType + "*";
	}
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((baseType == null) ? 0 : baseType.hashCode());
		result = prime * result + bits;
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
		LLPointerType other = (LLPointerType) obj;
		if (baseType == null) {
			if (other.baseType != null)
				return false;
		} else if (!baseType.equals(other.baseType))
			return false;
		if (bits != other.bits)
			return false;
		return true;
	}


	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.LLType#getBasicType()
	 */
	@Override
	public BasicType getBasicType() {
		return BasicType.POINTER;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.LLType#getBits()
	 */
	@Override
	public int getBits() {
		return bits;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.LLType#getSubType()
	 */
	@Override
	public LLType getSubType() {
		return baseType;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.LLType#isComplete()
	 */
	@Override
	public boolean isComplete() {
		return true;
	}

}
