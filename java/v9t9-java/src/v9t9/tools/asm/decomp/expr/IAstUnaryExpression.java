/**
 * 
 */
package v9t9.tools.asm.decomp.expr;

/**
 * An expression with a single operator.
 * 
 * @author eswartz
 *
 */
public interface IAstUnaryExpression extends IAstExpression {
    /** -expr (value 1) */
    public static final int K_NEGATE = 1;
    /** !expr (value 2) */
    public static final int K_NOT = 2;
    /** ~expr (value 3) */
    public static final int K_INVERT = 3;
    /** (expr) (value 4) */
    public static final int K_PARENTHESIS = 4;
    /** *expr (value 5) */
    public static final int K_INDIRECT = 5;
    /** Last value for subclass extensions */
    public static final int K_LAST = K_INDIRECT;
    
    /** Get the expression operator (K_xxx) */
    public int getOperator();
    
    /** Set the expression operator (K_xxx) */
    public void setOperator(int oper);

    /** Get the operand
     * 
     * @return IAstExpression (never null)
     */
    public IAstExpression getOperand();
    
    /** Set the operand 
     * 
     * @param expr (must not be null)
     */
    public void setOperand(IAstExpression expr);
}
