/**
 * 
 */
package org.ejs.eulang.ast;

/**
 * This node accesses the address of a pointer.
 * @author ejs
 *
 */
public interface IAstAddrRefExpr extends IAstTypedExpr {
	IAstAddrRefExpr copy(IAstNode copyParent);
	
	IAstTypedExpr getExpr();
	void setExpr(IAstTypedExpr expr);
}
