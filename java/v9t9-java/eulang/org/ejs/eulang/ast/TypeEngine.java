/**
 * 
 */
package org.ejs.eulang.ast;

import org.ejs.eulang.llvm.types.LLFloatType;
import org.ejs.eulang.llvm.types.LLIntType;
import org.ejs.eulang.llvm.types.LLType;

/**
 * @author ejs
 *
 */
public class TypeEngine {
	public LLType UNSPECIFIED = null;
	public LLIntType INT;
	public LLFloatType FLOAT;

	/**
	 * 
	 */
	public TypeEngine() {
		INT = new LLIntType(16);
		FLOAT = new LLFloatType(32);
	}
}
