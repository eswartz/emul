/**
 * 
 */
package org.ejs.eulang.ast;


/**
 * This is a node resulting from inlining a function.  This node
 * has no scope of its own.  #getResult()
 * yields the allocated symbol into which the result of the function
 * is stored.  #getStmtList yields the statements comprising the function,
 * where returns have been converted into assignments to the result
 * followed by jumps to the end.  All statements that originally referred
 * to locals now refer to new temporaries.
 * 
 * @author ejs
 *
 */
public interface IAstStmtListExpr extends IAstTypedExpr {
	IAstStmtListExpr copy(IAstNode copyParent);
	
	IAstSymbolExpr getResult();
	void setResult(IAstSymbolExpr result);
	void setStmtList(IAstNodeList<IAstStmt> list);
	IAstNodeList<IAstStmt> getStmtList();

}