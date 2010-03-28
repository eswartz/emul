/**
 * 
 */
package org.ejs.eulang.types;

/**
 * @author ejs
 *
 */
public class LLBoolType implements LLType {

	private final int bits;
	public LLBoolType(int bits) {
		this.bits = bits;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.LLType#getBasicType()
	 */
	@Override
	public BasicType getBasicType() {
		return BasicType.BOOL;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
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
		LLBoolType other = (LLBoolType) obj;
		if (bits != other.bits)
			return false;
		return true;
	}



	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "bool" + bits;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.types.LLType#getType()
	 */
	@Override
	public LLType getSubType() {
		return null;
	}

	/**
	 * @return
	 */
	public int getBits() {
		return bits;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.LLType#isComplete()
	 */
	@Override
	public boolean isComplete() {
		return true;
	}
}
