/**
 * 
 */
package org.ejs.eulang.ast.impl;

import java.util.ArrayList;
import java.util.List;

import org.ejs.eulang.ast.IAstExpr;
import org.ejs.eulang.ast.IAstExpressionList;
import org.ejs.eulang.ast.IAstNode;


/**
 * @author eswartz
 *
 */
public class AstExprList extends AstNode implements IAstExpressionList {

    List<IAstExpr> nodes;
    
    /**
     */
    public AstExprList() {
        super();
        nodes = new ArrayList<IAstExpr>();
        dirty = false;
    }

    
    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 8;
		result = prime * result + ((nodes == null) ? 0 : nodes.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;
		AstExprList other = (AstExprList) obj;
		if (nodes == null) {
			if (other.nodes != null)
				return false;
		} else if (!nodes.equals(other.nodes))
			return false;
		return true;
	}


	/* (non-Javadoc)
     * @see v9t9.tools.decomp.expr.IAstExpressionList#getList()
     */
    public IAstExpr[] getList() {
        return nodes.toArray(new IAstExpr[nodes.size()]);
    }

    /* (non-Javadoc)
     * @see v9t9.tools.decomp.expr.IAstExpressionList#addExpression(v9t9.tools.decomp.expr.IAstExpression)
     */
    public void addExpr(IAstExpr expr) {
        org.ejs.coffee.core.utils.Check.checkArg(expr);
        org.ejs.coffee.core.utils.Check.checkArg((!nodes.contains(expr)));
        nodes.add(expr);
        expr.setParent(this);
        dirty = true;
    }

    /* (non-Javadoc)
     * @see v9t9.tools.decomp.expr.IAstExpression#simplify()
     */
    public IAstExpr simplify() {
        return this;
    }

    /* (non-Javadoc)
     * @see v9t9.tools.decomp.expr.IAstExpression#equalValue(v9t9.tools.decomp.expr.IAstExpression)
     */
    public boolean equalValue(IAstExpr expr) {
        if (!(expr instanceof AstExprList)) {
			return false;
		}
        return ((AstExprList)expr).nodes.equals(nodes);
    }

    /* (non-Javadoc)
     * @see v9t9.tools.decomp.expr.IAstNode#getChildren()
     */
    public IAstNode[] getChildren() {
        return nodes.toArray(new IAstNode[nodes.size()]);
    }

    /* (non-Javadoc)
     * @see v9t9.tools.decomp.expr.IAstNode#getReferencedNodes()
     */
    public IAstNode[] getReferencedNodes() {
        return getChildren();
    }

}
