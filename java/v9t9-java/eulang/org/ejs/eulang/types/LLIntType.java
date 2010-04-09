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
}
