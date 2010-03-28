/**
 * 
 */
package org.ejs.eulang.ast;

import org.ejs.eulang.types.LLCodeType;


/**
 * @author ejs
 *
 */
public interface IAstPrototype extends IAstTypedNode {

	IAstType returnType();
	
	IAstArgDef[] argumentTypes();

	/**
	 * @return
	 */
	int getArgCount();

	/**
	 * @param newType
	 * @return TODO
	 */
	boolean adaptToType(LLCodeType newType);

}
