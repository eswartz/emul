/**
 * 
 */
package org.ejs.eulang.ast;

import org.ejs.eulang.IOperation;


/**
 * This node assigns one or more values to the location of the symbols.
 * 
 * Forms:  1 symbol expr and 1 expr<p>
 * 	N>1 symbol exprs and 1 expr:  if expand, re-evaluate expr for each symbol, else eval once and copy value<p> 
 * 	N>1 symbol exprs and N>! exprs: simultaneous assignment <p>
 * @author ejs
 *
 */
public interface IAstAssignStmt extends IAstStmt, IAstTypedExpr {
	IOperation getOperation();
	
	IAstAssignStmt copy(IAstNode copyParent);
	IAstNodeList<IAstTypedExpr> getSymbolExprs();
	void setSymbolExprs(IAstNodeList<IAstTypedExpr> id);
	IAstNodeList<IAstTypedExpr> getExprs();
	void setExprs(IAstNodeList<IAstTypedExpr> expr);

	void setExpand(boolean expand);
	boolean getExpand();
}
