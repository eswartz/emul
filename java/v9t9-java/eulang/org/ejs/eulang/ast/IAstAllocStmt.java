/**
 * 
 */
package org.ejs.eulang.ast;



/**
 * This node allocates a variable and optionally assigns an initial value.
 * @author ejs
 *
 */
public interface IAstAllocStmt extends IAstStmt, IAstTypedNode /*, IAstSymbolDefiner*/ {
	IAstAllocStmt copy(IAstNode copyParent);
	
	IAstNodeList<IAstSymbolExpr> getSymbolExpr();
	void setSymbolExpr(IAstNodeList<IAstSymbolExpr> id);
	
	IAstType getTypeExpr();
	void setTypeExpr(IAstType type);
	
	IAstNodeList<IAstTypedExpr> getExpr();
	void setExpr(IAstNodeList<IAstTypedExpr> expr);
	
	void setExpand(boolean expand);
	boolean getExpand();
}
