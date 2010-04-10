/**
 * 
 */
package org.ejs.eulang.types;

import org.ejs.eulang.types.LLType.BasicType;

/**
 * @author ejs
 *
 */
public class LLVoidType extends BaseLLType {


	public LLVoidType(String name) {
		super(name, 0, "void", BasicType.VOID, null);
		
	}
	@Override
	public boolean isComplete() {
		return true;
	}
	
}
