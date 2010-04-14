/**
 * 
 */
package org.ejs.eulang.ast;


/**
 * @author ejs
 *
 */
public interface IAstLabelStmt extends IAstStmt, IAstTypedExpr {
	IAstLabelStmt copy(IAstNode copyParent);
	IAstSymbolExpr getLabel();
	void setLabel(IAstSymbolExpr label);
}
