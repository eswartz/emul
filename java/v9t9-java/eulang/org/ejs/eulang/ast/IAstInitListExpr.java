/**
 * 
 */
package org.ejs.eulang.ast;

/**
 * An initializer list.
 * @author ejs
 *
 */
public interface IAstInitListExpr extends IAstInitNodeExpr {
	IAstInitListExpr copy(IAstNode parent);
	
	IAstNodeList<IAstInitNodeExpr> getInitExprs();
	void setInitExprs(IAstNodeList<IAstInitNodeExpr> exprs);
}
