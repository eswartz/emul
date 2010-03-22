/**
 * 
 */
package v9t9.tools.ast.expr;

/**
 * List of expressions (e.g. an array initializer).
 * The owner of this node should control the source formatting.
 * 
 * @author eswartz
 *
 */
public interface IAstExpressionList extends IAstExpression {
    /** Return ordered list of expression nodes */
    public IAstExpression[] getList();
    
    /** Add a node to the end of the expression list */
    public void addExpression(IAstExpression expr);
}
