/**
 * 
 */
package org.ejs.eulang.ast;

/**
 * @author ejs
 *
 */
public interface IAstIndexExpr extends IAstTypedExpr {
	IAstIndexExpr copy(IAstNode parent);
	
	IAstTypedExpr getExpr();
	void setExpr(IAstTypedExpr expr);
	IAstTypedExpr getIndex();
	void setIndex(IAstTypedExpr index);
}
