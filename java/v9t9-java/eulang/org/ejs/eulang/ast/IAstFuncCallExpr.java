/**
 * 
 */
package org.ejs.eulang.ast;

/**
 * @author ejs
 *
 */
public interface IAstFuncCallExpr extends IAstTypedExpr {
	IAstFuncCallExpr copy();
	IAstTypedExpr getFunction();
	void setFunction(IAstTypedExpr expr);
	IAstNodeList<IAstTypedExpr> arguments();
}
