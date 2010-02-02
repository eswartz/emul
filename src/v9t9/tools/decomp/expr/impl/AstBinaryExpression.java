/**
 * 
 */
package v9t9.tools.decomp.expr.impl;

import v9t9.tools.decomp.expr.IAstBinaryExpression;
import v9t9.tools.decomp.expr.IAstExpression;
import v9t9.tools.decomp.expr.IAstIntegralExpression;
import v9t9.tools.decomp.expr.IAstNode;

/**
 * @author eswartz
 *
 */
public class AstBinaryExpression extends AstExpression implements
        IAstBinaryExpression {

    protected int operator;
    protected IAstExpression left, right;
    
    /**
     * @param operator one of K_xxx
     * @param left left-hand side, must not be null
     * @param right right-hand side, must not be null
     */
    public AstBinaryExpression(int operator, IAstExpression left, IAstExpression right) {
        super();
        setOperator(operator);
        setLeftOperand(left);
        setRightOperand(right);
        dirty = false;
    }

    /* (non-Javadoc)
     * @see v9t9.tools.decomp.expr.impl.AstNode#toString()
     */
    @Override
	public String toString() {
        return "AstBinaryExpression { operator="+getOperatorName()+", left="+left+", right="+right+ " }"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }
    
    /* (non-Javadoc)
     * @see v9t9.tools.decomp.expr.impl.AstNode#getChildren()
     */
    public IAstNode[] getChildren() {
        return new IAstNode[] { left, right };
    }

    /* (non-Javadoc)
     * @see v9t9.tools.decomp.expr.IAstNode#getReferencedNodes()
     */
    public IAstNode[] getReferencedNodes() {
        return getChildren();
    }

    
    protected String getOperatorName() {
        char opText = 0;
        switch (operator) {
        case K_ADD:
            opText = '+';
            break;
         case K_AND:
            opText = '&';
            break;
        case K_DIV:
            opText = '/';
            break;
        case K_MOD:
            opText = '%';
            break;
        case K_MUL:
            opText = '*';
            break;
        case K_OR:
            opText = '|';
            break;
        case K_SUB:
            opText = '-';
            break;
        case K_SUBSCRIPT:
            opText = '[';
            break;
        case K_XOR:
            opText = '^';
            break;
        default:
            org.ejs.coffee.core.utils.Check.checkState(false);
        }
        return Character.toString(opText);
    }

    /* (non-Javadoc)
     * @see v9t9.tools.decomp.expr.IAstBinaryExpression#getOperator()
     */
    public int getOperator() {
        return operator;
    }

    /* (non-Javadoc)
     * @see v9t9.tools.decomp.expr.IAstBinaryExpression#setOperator(int)
     */
    public void setOperator(int operator) {
        org.ejs.coffee.core.utils.Check.checkArg((operator >= 0 && operator <= K_LAST));
        this.operator = operator;
        dirty = true;
    }

    /* (non-Javadoc)
     * @see v9t9.tools.decomp.expr.IAstBinaryExpression#getLeftOperand()
     */
    public IAstExpression getLeftOperand() {
        return left;
    }

    /* (non-Javadoc)
     * @see v9t9.tools.decomp.expr.IAstBinaryExpression#setLeftOperand(v9t9.tools.decomp.expr.IAstExpression)
     */
    public void setLeftOperand(IAstExpression expr) {
        org.ejs.coffee.core.utils.Check.checkArg(expr);
        if (this.left != null) {
			this.left.setParent(null);
		}
        this.left = expr;
        expr.setParent(this);
        dirty = true;
    }

    /* (non-Javadoc)
     * @see v9t9.tools.decomp.expr.IAstBinaryExpression#getRightOperand()
     */
    public IAstExpression getRightOperand() {
        return right;
    }

    /* (non-Javadoc)
     * @see v9t9.tools.decomp.expr.IAstBinaryExpression#setRightOperand(v9t9.tools.decomp.expr.IAstExpression)
     */
    public void setRightOperand(IAstExpression expr) {
        org.ejs.coffee.core.utils.Check.checkArg(expr);
        if (this.right != null) {
			this.right.setParent(null);
		}
        this.right = expr;
        expr.setParent(this);
        dirty = true;
    }

    /* (non-Javadoc)
     * @see v9t9.tools.decomp.expr.IAstExpression#simplify()
     */
    public IAstExpression simplify() {
        IAstExpression newLeft = left.simplify();
        IAstExpression newRight = right.simplify();
        
        // it is simplifiable?
        if (operator != K_SUBSCRIPT
                && newLeft instanceof IAstIntegralExpression
                && newRight instanceof IAstIntegralExpression) {
        
            IAstIntegralExpression litLeft = (IAstIntegralExpression) newLeft;
            IAstIntegralExpression litRight = (IAstIntegralExpression) newRight;
            
            switch (operator) {
            case K_ADD:
            case K_SUB:
            case K_MUL:
            case K_DIV: {
                    int intLeft = litLeft.getValue();
                    int intRight = litRight.getValue();
                    int intResult = 0;
                    switch (operator) {
                    case K_ADD:
                        intResult = intLeft + intRight; 
                        break;
                    case K_SUB:
                        intResult = intLeft - intRight; 
                        break;
                    case K_MUL:
                        intResult = intLeft * intRight; 
                        break;
                    case K_DIV:
                        intResult = intLeft / intRight; 
                        break;
                    }
                    IAstIntegralExpression lit = new AstIntegralExpression( 
                            intResult);
                    lit.setParent(getParent());
                    return lit;
                }
            
            case K_AND:
            case K_OR:
            case K_XOR:
            case K_MOD: {
                    int intLeft = litLeft.getValue();
                    int intRight = litRight.getValue();
                    int intResult = 0;
                    switch (operator) {
                    case K_AND:
                        intResult = intLeft & intRight; 
                        break;
                    case K_OR:
                        intResult = intLeft | intRight; 
                        break;
                    case K_XOR:
                        intResult = intLeft ^ intRight; 
                        break;
                    case K_MOD:
                        intResult = intLeft % intRight; 
                        break;
                    }
                    IAstIntegralExpression lit = new AstIntegralExpression(
                            intResult);
                    lit.setParent(getParent());
                    return lit;
            }
                }
        }
        
        // fallthrough: make new binary expression if children changed
        if (!newLeft.equalValue(left) || !newRight.equalValue(right)) {
            newLeft.setParent(null);
            newRight.setParent(null);
            IAstBinaryExpression bin = new AstBinaryExpression(operator, newLeft, newRight);
            bin.setParent(getParent());
            return bin;
        } else {
			return this;
		}
    }

    /* (non-Javadoc)
     * @see v9t9.tools.decomp.expr.IAstExpression#equalValue(v9t9.tools.decomp.expr.IAstExpression)
     */
    public boolean equalValue(IAstExpression expr) {
        if (!(expr instanceof IAstBinaryExpression)) {
			return false;
		}
        if (((IAstBinaryExpression) expr).getOperator() != getOperator()) {
			return false;
		}
        if (((IAstBinaryExpression) expr).getLeftOperand().equalValue(getLeftOperand())
        && ((IAstBinaryExpression) expr).getRightOperand().equalValue(getRightOperand())) {
			return true;
		}
        if (isCommutative() 
                && ((IAstBinaryExpression) expr).getLeftOperand().equalValue(getRightOperand())
                && ((IAstBinaryExpression) expr).getRightOperand().equalValue(getLeftOperand())) {
			return true;
		}
        return false;
    }

    /**
     * Tell whether the operator is commutative.
     */
    private boolean isCommutative() {
        return operator == K_ADD || operator == K_AND
            || operator == K_MUL || operator == K_OR 
            || operator == K_XOR;
    }

}
