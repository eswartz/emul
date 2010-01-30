/**
 * 
 */
package v9t9.tools.decomp.expr.impl;

import v9t9.tools.decomp.expr.IAstExpression;
import v9t9.tools.decomp.expr.IAstInitializerExpression;
import v9t9.tools.decomp.expr.IAstNode;

/**
 * @author eswartz
 *
 */
public class AstInitializerExpression extends AstInitializer implements
        IAstInitializerExpression {

    private IAstExpression expr;

    /**
     */
    public AstInitializerExpression(IAstExpression expr) {
        super();
        setExpression(expr);
        dirty = false;
    }

    /* (non-Javadoc)
     * @see v9t9.tools.decomp.expr.IAstInitializerExpression#getExpression()
     */
    public IAstExpression getExpression() {
        return expr;
    }

    /* (non-Javadoc)
     * @see v9t9.tools.decomp.expr.IAstInitializerExpression#setExpression(v9t9.tools.decomp.expr.IAstExpression)
     */
    public void setExpression(IAstExpression expr) {
        org.ejs.emul.core.utils.Check.checkArg(expr);
        if (this.expr != null) {
			this.expr.setParent(null);
		}
        if (this.expr == null || !this.expr.equalValue(expr)) {
			dirty = true;
		}
        this.expr = expr;
        expr.setParent(this);
    }

    /* (non-Javadoc)
     * @see v9t9.tools.decomp.expr.IAstNode#getChildren()
     */
    public IAstNode[] getChildren() {
        return new IAstNode[] { expr };
    }

    /* (non-Javadoc)
     * @see v9t9.tools.decomp.expr.IAstNode#getReferencedNodes()
     */
    public IAstNode[] getReferencedNodes() {
        return getChildren();
    }


}
