/**
 * 
 */
package v9t9.tools.asm.decomp.expr;


/**
 * A literal value
 * 
 * @author eswartz
 *
 */
public interface IAstIntegralExpression extends IAstExpression {
    /** Get the value (never null) */
    public int getValue();
    
    /** Set the value (cannot be null) 
     *
     * @throws IllegalArgumentException if value is not valid for kind
     */
    public void setValue(int value);
    
}
