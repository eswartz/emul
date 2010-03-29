/**
 * 
 */
package org.ejs.eulang.ast;


/**
 * This node allocates a variable and optionally assigns an initial value.
 * @author ejs
 *
 */
public interface IAstAllocStmt extends IAstDefineStmt, IAstTypedNode {
	IAstAllocStmt copy(IAstNode copyParent);
	
	IAstType getTypeExpr();
	void setTypeExpr(IAstType type);
}
