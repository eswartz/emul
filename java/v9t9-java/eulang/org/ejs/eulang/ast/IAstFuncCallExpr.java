/**
 * 
 */
package org.ejs.eulang.ast;

/**
 * @author ejs
 *
 */
public interface IAstFuncCallExpr extends IAstTypedExpr {
	IAstTypedExpr getFunction();
	void setFunction(IAstTypedExpr expr);
	IAstNodeList<IAstTypedExpr> arguments();
}
