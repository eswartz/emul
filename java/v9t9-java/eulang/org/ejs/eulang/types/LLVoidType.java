/**
 * 
 */
package org.ejs.eulang.types;

/**
 * @author ejs
 *
 */
public class LLVoidType extends BaseLLType {


	public LLVoidType(String name) {
		super(name, 0, "void", BasicType.VOID);
		
	}
	@Override
	public boolean isComplete() {
		return true;
	}
	
}
