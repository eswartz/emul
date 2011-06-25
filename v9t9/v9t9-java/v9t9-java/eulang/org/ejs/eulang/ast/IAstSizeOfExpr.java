/**
 * 
 */
package org.ejs.eulang.ast;

/**
 * This node returns the storage size of a type or expression.
 * @author ejs
 *
 */
public interface IAstSizeOfExpr extends IAstTypedExpr {
	IAstSizeOfExpr copy();
	
	IAstTypedExpr getExpr();
	void setExpr(IAstTypedExpr node);
}
