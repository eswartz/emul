/**
 * 
 */
package org.ejs.eulang.ast;

/**
 * A counted 'repeat' loop
 * @author ejs
 *
 */
public interface IAstRepeatExpr extends IAstLoopStmt {
	 IAstRepeatExpr copy(IAstNode copyParent);
	 
	IAstTypedExpr getExpr();
	void setExpr(IAstTypedExpr expr);
}
