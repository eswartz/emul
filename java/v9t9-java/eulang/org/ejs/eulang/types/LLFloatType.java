/**
 * 
 */
package org.ejs.eulang.types;

/**
 * @author ejs
 *
 */
public class LLFloatType implements LLType {

	private final int bits;
	private final int mbits;
	public LLFloatType(int bits, int mbits) {
		this.bits = bits;
		this.mbits = mbits;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.LLType#getBasicType()
	 */
	@Override
	public BasicType getBasicType() {
		return BasicType.FLOATING;
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
		LLFloatType other = (LLFloatType) obj;
		if (bits != other.bits)
			return false;
		return true;
	}



	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "float" + bits;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.types.LLType#getType()
	 */
	@Override
	public LLType getSubType() {
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.LLType#getBits()
	 */
	@Override
	public int getBits() {
		return bits;
	}

	public int getMantissaBits() {
		return mbits;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.LLType#isComplete()
	 */
	@Override
	public boolean isComplete() {
		return true;
	}
}
