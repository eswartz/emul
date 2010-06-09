/**
 * 
 */
package org.ejs.eulang.ast;

/**
 * This node returns the type of a given type or expression.
 * @author ejs
 *
 */
public interface IAstTypeOfExpr extends IAstType {
	IAstTypeOfExpr copy();
	
	IAstTypedExpr getExpr();
	void setExpr(IAstTypedExpr node);
}
