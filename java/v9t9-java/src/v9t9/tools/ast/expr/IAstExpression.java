/**
 * 
 */
package v9t9.tools.ast.expr;

/**
 * Root interface for expression nodes
 * 
 * @author eswartz
 *
 */
public interface IAstExpression extends IAstNode {
    /** 
     * Get a simplified version of the expression, by removing
     * meaningless nodes and attempting to replace constant operations 
     * with literal values (IAstLiteralExpression).
     * <p>
     * This does NOT necessarily return a unique IAstExpression!
     * 
     * @return IAstExpression
     */
    public IAstExpression simplify();
    
    /** 
     * Compare equality, value-wise.  This assumes the
     * expressions are of the same structure -- call
     * simplify() first if necessary.
     * 
     * @see #simplify()
     */
    public boolean equalValue(IAstExpression expr);
}
