/**
 * 
 */
package org.ejs.eulang.ast;

/**
 * List of expressions (e.g. an array initializer).
 * The owner of this node should control the source formatting.
 * 
 * @author eswartz
 *
 */
public interface IAstExpressionList extends IAstExpr {
    /** Return ordered list of expression nodes */
    public IAstExpr[] getList();
    
    /** Add a node to the end of the expression list */
    public void addExpr(IAstExpr expr);
}
