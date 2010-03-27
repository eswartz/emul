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
     * Get a simplified version of the expression, by removing
     * meaningless nodes and attempting to replace constant operations 
     * with literal values (IAstLiteralExpression).
     * <p>
     * This does NOT necessarily return a unique IAstExpression!
     * 
     * @return IAstExpression
     */
    public IAstExpr simplify();
    
    /** 
     * Compare equality, value-wise.  This assumes the
     * expressions are of the same structure -- call
     * simplify() first if necessary.
     * 
     * @see #simplify()
     */
    public boolean equalValue(IAstExpr expr);
}
