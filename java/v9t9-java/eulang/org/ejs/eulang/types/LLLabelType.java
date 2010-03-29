/**
 * 
 */
package org.ejs.eulang.types;

/**
 * @author ejs
 *
 */
public class LLLabelType implements LLType {

	public LLLabelType() {
		
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "label";
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.LLType#getBasicType()
	 */
	@Override
	public BasicType getBasicType() {
		return BasicType.CODE;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.LLType#getBits()
	 */
	@Override
	public int getBits() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.LLType#getSubType()
	 */
	@Override
	public LLType getSubType() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.LLType#isComplete()
	 */
	@Override
	public boolean isComplete() {
		return true;
	}

}
