/**
 * 
 */
package org.ejs.eulang.ast;


/**
 * This node allocates a variable and optionally assigns an initial value.
 * @author ejs
 *
 */
public interface IAstAllocStmt extends IAstStatement, IAstTypedExpr,
		IAstExpr {

	IAstSymbolExpr getId();
	void setId(IAstSymbolExpr id);
	IAstType getTypeExpr();
	void setTypeExpr(IAstType type);
	IAstTypedExpr getExpr();
	void setExpr(IAstTypedExpr expr);
}
