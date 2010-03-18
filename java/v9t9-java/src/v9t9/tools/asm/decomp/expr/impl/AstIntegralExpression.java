/**
 * 
 */
package v9t9.tools.asm.decomp.expr.impl;

import v9t9.tools.asm.decomp.expr.IAstExpression;
import v9t9.tools.asm.decomp.expr.IAstIntegralExpression;
import v9t9.tools.asm.decomp.expr.IAstNode;

/**
 * @author eswartz
 *
 */
public class AstIntegralExpression extends AstExpression implements
        IAstIntegralExpression {

    protected int kind;
    protected int value;

    public AstIntegralExpression(int value) {
        super();
        setValue(value);
        dirty = false;
    }

    /* (non-Javadoc)
     * @see v9t9.tools.decomp.expr.IAstNode#getChildren()
     */
    public IAstNode[] getChildren() {
        return NO_CHILDREN;
    }

    /* (non-Javadoc)
     * @see v9t9.tools.decomp.expr.IAstNode#getReferencedNodes()
     */
    public IAstNode[] getReferencedNodes() {
        return getChildren();
    }


    /* (non-Javadoc)
     * @see v9t9.tools.decomp.expr.IAstLiteralExpression#getValue()
     */
    public int getValue() {
        return value;
    }

    /* (non-Javadoc)
     * @see v9t9.tools.decomp.expr.IAstLiteralExpression#setValue(java.lang.String)
     */
    public void setValue(int value) {
        if (this.value != value) {
        	    dirty = true;
        }
        this.value = value;
    }

    
    /* (non-Javadoc)
     * @see v9t9.tools.decomp.expr.IAstExpression#simplify()
     */
    public IAstExpression simplify() {
        return this;
    }
    
     /* (non-Javadoc)
     * @see v9t9.tools.decomp.expr.IAstExpression#equalValue(v9t9.tools.decomp.expr.IAstExpression)
     */
    public boolean equalValue(IAstExpression expr) {
        return expr instanceof IAstIntegralExpression
        && ((IAstIntegralExpression) expr).getValue() == getValue();
    }
}
