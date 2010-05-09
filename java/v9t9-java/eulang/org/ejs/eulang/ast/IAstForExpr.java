/**
 * 
 */
package org.ejs.eulang.ast;

/**
 *	A for loop iterates on an iterable, which can be an integer (for now)
 */
public interface IAstForExpr extends IAstTestBodyLoopExpr {
	 IAstForExpr copy(IAstNode copyParent);
	 
	 IAstNodeList<IAstSymbolExpr> getSymbolExprs();
	 void setSymbolExprs(IAstNodeList<IAstSymbolExpr> expr);
}
