/**
 * 
 */
package org.ejs.eulang.ast;

/**
 * @author ejs
 *
 */
public interface IAstTupleNode extends IAstNode {
	IAstTupleNode copy();
	IAstNodeList<IAstTypedExpr> elements();
}
