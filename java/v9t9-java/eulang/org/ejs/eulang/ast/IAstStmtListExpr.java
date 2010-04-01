/**
 * 
 */
package org.ejs.eulang.ast;


/**
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