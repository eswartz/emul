/**
 * 
 */
package org.ejs.eulang.types;

/**
 * @author ejs
 *
 */
public class LLRefType implements LLType {

	private LLType subType;
	private final int ptrBits;

	public LLRefType(LLType baseType, int ptrBits) {
		this.ptrBits = ptrBits;
		if (baseType instanceof LLRefType)
			throw new IllegalArgumentException();
		this.subType = baseType;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "&" + (subType != null ? subType.toString() : "");
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ptrBits;
		result = prime * result + ((subType == null) ? 0 : subType.hashCode());
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
		LLRefType other = (LLRefType) obj;
		if (ptrBits != other.ptrBits)
			return false;
		if (subType == null) {
			if (other.subType != null)
				return false;
		} else if (!subType.equals(other.subType))
			return false;
		return true;
	}


	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.LLType#getBasicType()
	 */
	@Override
	public BasicType getBasicType() {
		return BasicType.REF;
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
		return subType;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.LLType#isComplete()
	 */
	@Override
	public boolean isComplete() {
		return subType != null;
	}

}
