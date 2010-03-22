/**
 * 
 */
package v9t9.tools.ast.expr.impl;

import v9t9.tools.ast.expr.IAstExpression;
import v9t9.tools.ast.expr.IAstIntegralExpression;
import v9t9.tools.ast.expr.IAstNode;
import v9t9.tools.ast.expr.IAstUnaryExpression;

/**
 * @author eswartz
 *
 */
public class AstUnaryExpression extends AstExpression implements
        IAstUnaryExpression {

    protected IAstExpression operand;
    protected int operator;

    /** Create a unary expression
     * 
     * @param operator one of K_xxx
     * @param operand must not be null
     */
    public AstUnaryExpression(int operator, IAstExpression operand) {
        super();
        setOperator(operator);
        setOperand(operand);
        dirty = false;
    }

    /* (non-Javadoc)
     * @see v9t9.tools.decomp.expr.impl.AstNode#toString()
     */
    @Override
	public String toString() {
        return "AstUnaryExpression { operator="+getOperatorName()+", operand="+operand + " }"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
    
     /* (non-Javadoc)
     * @see v9t9.tools.decomp.expr.impl.AstNode#getChildren()
     */
    public IAstNode[] getChildren() {
        return new IAstNode[] { operand };
    }

    /*
     * (non-Javadoc)
     *  
     * @see v9t9.tools.decomp.expr.IAstNode#getReferencedNodes()
     */
     public IAstNode[] getReferencedNodes() {
         return getChildren();
     }

     protected String getOperatorName() {
         String opString = "???"; //$NON-NLS-1$
         switch (operator) {
         case K_NEGATE:
             opString = "-"; //$NON-NLS-1$
             break;
         case K_NOT:
             opString = "!"; //$NON-NLS-1$
             break;
         case K_INVERT:
             opString = "~"; //$NON-NLS-1$
             break;
         case K_PARENTHESIS:
             opString = "("; //$NON-NLS-1$
             break;
         case K_INDIRECT:
             opString = "*"; //$NON-NLS-1$
             break;
         default:
             org.ejs.coffee.core.utils.Check.checkState(false);
         }
         return opString;
     
     }

    /* (non-Javadoc)
     * @see v9t9.tools.decomp.expr.IAstUnaryExpression#getOperator()
     */
    public int getOperator() {
        return operator;
    }

    /* (non-Javadoc)
     * @see v9t9.tools.decomp.expr.IAstUnaryExpression#setOperator(int)
     */
    public void setOperator(int oper) {
        org.ejs.coffee.core.utils.Check.checkArg((oper >= 0 && oper <= K_LAST));
        this.operator = oper;
        dirty = true;
    }

    /* (non-Javadoc)
     * @see v9t9.tools.decomp.expr.IAstUnaryExpression#getOperand()
     */
    public IAstExpression getOperand() {
        return operand;
    }

    /* (non-Javadoc)
     * @see v9t9.tools.decomp.expr.IAstUnaryExpression#setOperand(v9t9.tools.decomp.expr.IAstExpression)
     */
    public void setOperand(IAstExpression expr) {
        org.ejs.coffee.core.utils.Check.checkArg(expr);
        if (this.operand != null) {
			this.operand.setParent(null);
		}
        this.operand = expr;
        expr.setParent(this);
        dirty = true;
    }

    /* (non-Javadoc)
     * @see v9t9.tools.decomp.expr.IAstExpression#simplify()
     */
    public IAstExpression simplify() {
        
        IAstExpression expr = operand.simplify();

        if (operator == K_PARENTHESIS) {
			return expr;
		}

        if (expr instanceof IAstIntegralExpression) {
            IAstIntegralExpression lit = (IAstIntegralExpression) expr;
                
            int val = lit.getValue();
            
            switch (operator) {
            case K_NEGATE:
                val = -val;
                break;
            case K_NOT:
                val = val != 0 ? 0 : 1;
                break;
            case K_INVERT:
                val = ~val;
                break;
            case K_INDIRECT:
                return this;
            }

            IAstIntegralExpression lex = new AstIntegralExpression(val); 
            lex.setParent(getParent());
            return lex;
        }
        
        if (!expr.equalValue(operand)) {
            expr.setParent(null);
            IAstUnaryExpression un = new AstUnaryExpression(operator, expr);
            un.setParent(getParent());
            return un;
        } else {
			return this;
		}
    }
    
    /* (non-Javadoc)
     * @see v9t9.tools.decomp.expr.IAstExpression#equalValue(v9t9.tools.decomp.expr.IAstExpression)
     */
    public boolean equalValue(IAstExpression expr) {
        return expr instanceof IAstUnaryExpression
        && ((IAstUnaryExpression) expr).getOperator() == getOperator()
        && ((IAstUnaryExpression) expr).getOperand().equalValue(getOperand());
    }
}
