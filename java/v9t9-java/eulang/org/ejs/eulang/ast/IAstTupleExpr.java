/**
 * 
 */
package org.ejs.eulang.ast;

/**
 * @author ejs
 *
 */
public interface IAstTupleExpr extends IAstTypedExpr {
	IAstTupleExpr copy(IAstNode parent);
	IAstNodeList<IAstTypedExpr> elements();
}
