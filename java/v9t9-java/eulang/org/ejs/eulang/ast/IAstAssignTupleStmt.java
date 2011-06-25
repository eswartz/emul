/**
 * 
 */
package org.ejs.eulang.ast;


/**
 * This node unpacks and assigns new values to the locations of the ids.
 * @author ejs
 *
 */
public interface IAstAssignTupleStmt extends IAstStmt, IAstTypedExpr {
	IAstAssignTupleStmt copy();
	IAstTupleNode getSymbols();
	void setSymbols(IAstTupleNode id);
	IAstTypedExpr getExpr();
	void setExpr(IAstTypedExpr expr);
}
