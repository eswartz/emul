/**
 * 
 */
package org.ejs.eulang.ast;


/**
 * @author ejs
 *
 */
public interface IAstLabelStmt extends IAstStmt, IAstTypedExpr {
	IAstLabelStmt copy();
	IAstSymbolExpr getLabel();
	void setLabel(IAstSymbolExpr label);
}
