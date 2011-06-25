/**
 * 
 */
package org.ejs.eulang.ast;


/**
 * A list of expressions, where the last expression yields the value.
 *  #getStmtList yields the statements comprising the function,
 * where returns have been converted into assignments to the result
 * followed by jumps to the end.  All statements that originally referred
 * to locals now refer to new temporaries.
 * 
 * @author ejs
 *
 */
public interface IAstStmtListExpr extends IAstTypedExpr {
	IAstStmtListExpr copy();
	
	IAstTypedExpr getValue();
	
	//IAstSymbolExpr getResult();
	//void setResult(IAstSymbolExpr result);
	void setStmtList(IAstNodeList<IAstStmt> list);
	IAstNodeList<IAstStmt> getStmtList();

}