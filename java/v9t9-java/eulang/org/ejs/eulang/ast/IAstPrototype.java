/**
 * 
 */
package org.ejs.eulang.ast;


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

}
