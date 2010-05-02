/**
 * 
 */
package org.ejs.eulang.types;

import org.ejs.eulang.TypeEngine;

/**
 * @author ejs
 *
 */
public class LLFloatType extends BaseLLType {

	private final int mbits;
	public LLFloatType(String name, int bits, int mbits) {
		super(name, bits, bits==32 ? "float" : bits==64 ? "double" : null, BasicType.FLOATING, null);
		this.mbits = mbits;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + mbits;
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
		if (mbits != other.mbits)
			return false;
		return true;
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
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.LLType#updateTypes(org.ejs.eulang.TypeEngine, org.ejs.eulang.types.LLType[])
	 */
	@Override
	public LLType updateTypes(TypeEngine typeEngine, LLType[] type) {
		return this;
	}
}
