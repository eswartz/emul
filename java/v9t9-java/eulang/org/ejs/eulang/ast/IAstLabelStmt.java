/**
 * 
 */
package org.ejs.eulang.ast;


/**
 * @author ejs
 *
 */
public interface IAstLabelStmt extends IAstStatement {
	IAstSymbolExpr getLabel();
	void setLabel(IAstSymbolExpr label);
}
