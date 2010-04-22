/**
 * 
 */
package org.ejs.eulang.ast;

/**
 * @author ejs
 *
 */
public interface IAstArrayType extends IAstType {
	IAstArrayType copy(IAstNode copyParent);
	
	IAstType getBaseType();
	void setBaseType(IAstType typeExpr);
	IAstTypedExpr getCount();
	void setCount(IAstTypedExpr countExpr);
}
