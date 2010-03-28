/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.eulang.ast.IAstExpr;
import org.ejs.eulang.ast.IAstInitializerExpr;
import org.ejs.eulang.ast.IAstNode;

/**
 * @author eswartz
 *
 */
public class AstInitializerExpr extends AstInitializer implements
        IAstInitializerExpr {

    private IAstExpr expr;

    /**
     */
    public AstInitializerExpr(IAstExpr expr) {
        super();
        setExpr(expr);
        dirty = false;
    }
    
    

    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 6;
		result = prime * result + ((expr == null) ? 0 : expr.hashCode());
		return result;
	}



	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;
		AstInitializerExpr other = (AstInitializerExpr) obj;
		if (expr == null) {
			if (other.expr != null)
				return false;
		} else if (!expr.equals(other.expr))
			return false;
		return true;
	}



	/* (non-Javadoc)
     * @see v9t9.tools.decomp.expr.IAstInitializerExpression#getExpression()
     */
    public IAstExpr getExpr() {
        return expr;
    }

    /* (non-Javadoc)
     * @see v9t9.tools.decomp.expr.IAstInitializerExpression#setExpression(v9t9.tools.decomp.expr.IAstExpression)
     */
    public void setExpr(IAstExpr expr) {
        org.ejs.coffee.core.utils.Check.checkArg(expr);
        this.expr = reparent(this.expr, expr);
        dirty = true;
    }

    /* (non-Javadoc)
     * @see v9t9.tools.decomp.expr.IAstNode#getChildren()
     */
    public IAstNode[] getChildren() {
        return new IAstNode[] { expr };
    }
}
