/**
 * 
 */
package org.ejs.eulang.types;

/**
 * @author ejs
 *
 */
public class LLPointerType extends BaseLLType {

	
	public LLPointerType(String name, int bits, LLType baseType) {
		super(name, bits, baseType.getLLVMType() + "*", BasicType.POINTER, baseType);
		
	}
	public LLPointerType(int bits, LLType baseType) {
		super(baseType.getName() + "$p", bits, baseType.getLLVMType() + "*", BasicType.POINTER, baseType);
		
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.LLType#isComplete()
	 */
	@Override
	public boolean isComplete() {
		return true;
	}

}
