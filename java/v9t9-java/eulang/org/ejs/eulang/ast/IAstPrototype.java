/**
 * 
 */
package org.ejs.eulang.ast;

import org.ejs.eulang.types.LLCodeType;


/**
 * @author ejs
 *
 */
public interface IAstPrototype extends IAstNode {
	IAstPrototype copy(IAstNode copyParent);
	
	IAstType returnType();
	
	IAstArgDef[] argumentTypes();

	int getArgCount();
	boolean adaptToType(LLCodeType newType);

	/**
	 * @return
	 */
	boolean hasDefaultArguments();

}
