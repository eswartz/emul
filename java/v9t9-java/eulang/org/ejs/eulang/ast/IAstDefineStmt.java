/**
 * 
 */
package org.ejs.eulang.ast;

import java.util.Map;

import org.ejs.eulang.symbols.ISymbol;


/**
 * This node defines a name to a node.  This means a variant of the node may be 
 * substituted for the name.
 * @author ejs
 *
 */
public interface IAstDefineStmt extends IAstStmt {
	IAstDefineStmt copy(IAstNode copyParent);
	IAstSymbolExpr getSymbolExpr();
	void setSymbolExpr(IAstSymbolExpr id);
	ISymbol getSymbol();
	IAstTypedExpr getExpr();
	void setExpr(IAstTypedExpr expr);
	
	Map<ISymbol, IAstTypedExpr> expansions();
}
