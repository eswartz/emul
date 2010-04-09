/**
 * 
 */
package org.ejs.eulang.types;

/**
 * @author ejs
 *
 */
public class LLBoolType extends BaseLLType  {

	public LLBoolType(String name, int bits) {
		super(name, bits, "i" + bits, BasicType.BOOL, null);
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.LLType#isComplete()
	 */
	@Override
	public boolean isComplete() {
		return true;
	}
}
