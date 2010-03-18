/**
 * 
 */
package v9t9.tools.asm.decomp.expr;


/**
 * An identifier used in an expression 
 * 
 * @author eswartz
 *
 */
public interface IAstIdExpression extends IAstExpression, IAstNameHolder {
    /** Get the name referenced */
    public IAstName getName();
    
    /** Set the name */
    public void setName(IAstName name);
}
