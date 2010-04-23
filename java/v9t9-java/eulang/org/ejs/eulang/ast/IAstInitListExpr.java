/**
 * 
 */
package org.ejs.eulang.ast;

/**
 * An initializer list.
 * @author ejs
 *
 */
public interface IAstInitListExpr extends IAstTypedExpr {
	IAstInitListExpr copy(IAstNode parent);
	
	IAstNodeList<IAstInitNodeExpr> getInitExprs();
	void setInitExprs(IAstNodeList<IAstInitNodeExpr> exprs);
}
