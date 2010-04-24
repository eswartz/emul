/**
 * 
 */
package org.ejs.eulang.ast;

/**
 * This node on an identifier expresses the intent to get at the base value of
 * the identifier.  E.g. an Int is Int, an Int^ is Int, an Int& is Int, an Int^^ is Int. 
 * @author ejs
 *
 */
public interface IAstValueExpr extends IAstTypedExpr {
	IAstValueExpr copy(IAstNode copyParent);
	
	IAstTypedExpr getExpr();
	void setExpr(IAstTypedExpr expr);
}
