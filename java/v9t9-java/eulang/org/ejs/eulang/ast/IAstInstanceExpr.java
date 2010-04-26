/**
 * 
 */
package org.ejs.eulang.ast;

/**
 * @author ejs
 *
 */
public interface IAstInstanceExpr extends IAstTypedExpr, IAstType {
	IAstInstanceExpr copy(IAstNode parent);
	
	IAstSymbolExpr getSymbolExpr();
	void setSymbolExpr(IAstSymbolExpr symExpr);
	IAstNodeList<IAstTypedExpr> getExprs();
	void setExprs(IAstNodeList<IAstTypedExpr> list);
	
}
