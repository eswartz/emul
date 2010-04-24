/**
 * 
 */
package org.ejs.eulang.ast;

/**
 * This node accesses the address of anything.
 * @author ejs
 *
 */
public interface IAstAddrOfExpr extends IAstTypedExpr {
	IAstAddrOfExpr copy(IAstNode copyParent);
	
	IAstTypedExpr getExpr();
	void setExpr(IAstTypedExpr expr);
}
