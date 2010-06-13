/**
 * 
 */
package org.ejs.eulang.ast;



/**
 * This node allocates several variables and optionally assigns initial values.
 * @author ejs
 *
 */
public interface IAstAllocTupleStmt extends IAstStmt, IAstTypedNode, IAstAttributes {
	IAstAllocTupleStmt copy();
	
	IAstTupleNode getSymbols();
	void setSymbols(IAstTupleNode id);
	IAstTypedExpr getExpr();
	void setExpr(IAstTypedExpr expr);
	
	IAstType getTypeExpr();
	void setTypeExpr(IAstType type);
}
