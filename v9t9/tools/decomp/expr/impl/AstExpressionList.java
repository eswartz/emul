/**
 * 
 */
package v9t9.tools.decomp.expr.impl;

import java.util.ArrayList;
import java.util.List;

import v9t9.tools.decomp.expr.IAstExpression;
import v9t9.tools.decomp.expr.IAstExpressionList;
import v9t9.tools.decomp.expr.IAstNode;
import v9t9.utils.Check;

/**
 * @author eswartz
 *
 */
public class AstExpressionList extends AstNode implements IAstExpressionList {

    List<IAstExpression> nodes;
    
    /**
     */
    public AstExpressionList() {
        super();
        nodes = new ArrayList<IAstExpression>();
        dirty = false;
    }

    /* (non-Javadoc)
     * @see v9t9.tools.decomp.expr.IAstExpressionList#getList()
     */
    public IAstExpression[] getList() {
        return nodes.toArray(new IAstExpression[nodes.size()]);
    }

    /* (non-Javadoc)
     * @see v9t9.tools.decomp.expr.IAstExpressionList#addExpression(v9t9.tools.decomp.expr.IAstExpression)
     */
    public void addExpression(IAstExpression expr) {
        Check.checkArg(expr);
        Check.checkArg(!nodes.contains(expr));
        nodes.add(expr);
        expr.setParent(this);
        dirty = true;
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
        if (!(expr instanceof AstExpressionList)) {
			return false;
		}
        return ((AstExpressionList)expr).nodes.equals(nodes);
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
