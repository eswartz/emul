/**
 * 
 */
package org.ejs.eulang.ast;




/**
 * @author ejs
 *
 */
public interface IAstTypedExpr extends IAstTypedNode, IAstExpr {
    /** 
     * Get a simplified version of the expression, by removing
     * meaningless nodes and attempting to replace constant operations 
     * with literal values (IAstLiteralExpression).
     * <p>
     * This does NOT necessarily return a unique IAstExpression!
     * 
     * @return IAstExpression
     */
    public IAstExpr simplify(TypeEngine engine);
    

}
