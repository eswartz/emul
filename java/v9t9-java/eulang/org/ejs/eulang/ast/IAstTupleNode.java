/**
 * 
 */
package org.ejs.eulang.ast;

/**
 * @author ejs
 *
 */
public interface IAstTupleNode extends IAstNode {
	IAstTupleNode copy(IAstNode parent);
	IAstNodeList<IAstTypedExpr> elements();
}
