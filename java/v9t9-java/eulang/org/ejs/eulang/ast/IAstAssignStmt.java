/**
 * 
 */
package org.ejs.eulang.ast;


/**
 * This node assigns a new value to the location of the id.
 * @author ejs
 *
 */
public interface IAstAssignStmt extends IAstStmt, IAstTypedExpr {
	IAstAssignStmt copy(IAstNode copyParent);
	IAstNodeList<IAstSymbolExpr> getSymbol();
	void setSymbol(IAstNodeList<IAstSymbolExpr> id);
	IAstNodeList<IAstTypedExpr> getExpr();
	void setExpr(IAstNodeList<IAstTypedExpr> expr);

	void setExpand(boolean expand);
	boolean getExpand();
}
