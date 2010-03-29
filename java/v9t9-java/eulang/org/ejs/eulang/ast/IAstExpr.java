/**
 * 
 */
package org.ejs.eulang.ast;

/**
 * Root interface for expression nodes
 * 
 * @author eswartz
 *
 */
public interface IAstExpr extends IAstNode {
    /** 
     * Compare equality, value-wise.  This assumes the
     * expressions are of the same structure -- call
     * simplify() first if necessary.
     * 
     * @see #simplify()
     */
    public boolean equalValue(IAstExpr expr);
}
