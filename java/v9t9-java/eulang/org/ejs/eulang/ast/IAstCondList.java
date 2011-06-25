/**
 * 
 */
package org.ejs.eulang.ast;

/**
 * @author ejs
 *
 */
public interface IAstCondList extends IAstTypedExpr {
	IAstCondList copy();
	
	IAstNodeList<IAstCondExpr> getCondExprs();
	void setCondExprs(IAstNodeList<IAstCondExpr> exprList);
}
