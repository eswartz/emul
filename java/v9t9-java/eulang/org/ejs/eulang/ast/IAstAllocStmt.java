/**
 * 
 */
package org.ejs.eulang.ast;



/**
 * This node allocates one or more variables and optionally assigns initial value(s).
 * 
 *  Forms:  1 symbol expr and 1 expr<p>
 * 	N>1 symbol exprs and 1 expr:  if expand, re-evaluate expr for each symbol, else eval once and copy value<p> 
 * 	N>1 symbol exprs and N>! exprs: simultaneous assignment <p>

 * @author ejs
 *
 */
public interface IAstAllocStmt extends IAstStmt, IAstTypedNode /*, IAstSymbolDefiner*/ {
	IAstAllocStmt copy(IAstNode copyParent);
	
	IAstNodeList<IAstSymbolExpr> getSymbolExprs();
	void setSymbolExprs(IAstNodeList<IAstSymbolExpr> id);
	
	IAstType getTypeExpr();
	void setTypeExpr(IAstType type);
	
	IAstNodeList<IAstTypedExpr> getExprs();
	void setExprs(IAstNodeList<IAstTypedExpr> expr);
	
	void setExpand(boolean expand);
	boolean getExpand();
}
