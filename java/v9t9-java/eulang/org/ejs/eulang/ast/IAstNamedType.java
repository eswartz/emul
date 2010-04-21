/**
 * 
 */
package org.ejs.eulang.ast;

/**
 * @author ejs
 *
 */
public interface IAstNamedType extends IAstType {
	IAstNamedType copy(IAstNode copyParent);
	IAstSymbolExpr getSymbol();
	void setSymbol(IAstSymbolExpr symbolExpr);
}
