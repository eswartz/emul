/**
 * 
 */
package v9t9.tools.decomp.expr;

/**
 * An expression defining an initializer.  This implicitly contains
 * an "=".
 * @author eswartz
 *
 */
public interface IAstInitializerExpression extends IAstInitializer {
    /** Get the initializer expression (never null) */
    public IAstExpression getExpression();
    
    /** Set the initializer expression (must not be null) */
    public void setExpression(IAstExpression expr);
}
