/**
 * 
 */
package org.ejs.eulang.types;

/**
 * @author ejs
 *
 */
public class LLIntType extends BaseLLType {

	public LLIntType(String name, int bits) {
		super(name, bits, "i" + bits, BasicType.INTEGRAL, null);
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.LLType#isComplete()
	 */
	@Override
	public boolean isComplete() {
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.BaseLLType#hashCode()
	 */
	@Override
	public int hashCode() {
		int prime = 57;
		int val = getBits() * prime;
		return val;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.BaseLLType#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LLIntType other = (LLIntType) obj;
		return other.basicType == basicType && other.bits == bits;
	}
}
