/**
 * 
 */
package org.ejs.eulang.ast;

/**
 * @author ejs
 *
 */
public interface IAstTupleExpr extends IAstTypedExpr {
	IAstTupleExpr copy();
	IAstNodeList<IAstTypedExpr> elements();
}
