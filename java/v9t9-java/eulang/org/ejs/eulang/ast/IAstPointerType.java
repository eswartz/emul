/**
 * 
 */
package org.ejs.eulang.ast;

/**
 * @author ejs
 *
 */
public interface IAstPointerType extends IAstType {
	IAstPointerType copy(IAstNode copyParent);
	
	IAstType getBaseType();
	void setBaseType(IAstType typeExpr);
}
