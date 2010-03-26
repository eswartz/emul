/**
 * 
 */
package v9t9.tools.ast.expr.impl;

import v9t9.tools.ast.expr.IAstExpression;
import v9t9.tools.ast.expr.IAstIdExpression;
import v9t9.tools.ast.expr.IAstName;
import v9t9.tools.ast.expr.IAstNode;

/**
 * @author eswartz
 *
 */
public class AstIdExpression extends AstExpression implements IAstIdExpression {

   /** name referenced in expression */
    private IAstName name;

    public AstIdExpression(IAstName name) {
        super();
        setName(name);
        dirty = false;
    }

    /* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.impl.AstNode#toString()
	 */
	@Override
	public String toString() {
		return name.toString();
	}
    	
    /* (non-Javadoc)
     * @see v9t9.tools.decomp.expr.IAstNode#getChildren()
     */
    public IAstNode[] getChildren() {
        // name is not owned
        return NO_CHILDREN;
    }
    
    /* (non-Javadoc)
     * @see v9t9.tools.decomp.expr.IAstNode#getReferencedNodes()
     */
    public IAstNode[] getReferencedNodes() {
        return new IAstNode[] { name };
    }

    
    /* (non-Javadoc)
     * @see v9t9.tools.decomp.expr.IAstIdExpression#getName()
     */
    public IAstName getName() {
        return name;
    }

    /* (non-Javadoc)
     * @see v9t9.tools.decomp.expr.IAstIdExpression#setName(v9t9.tools.decomp.expr.IAstName)
     */
    public void setName(IAstName name) {
        org.ejs.coffee.core.utils.Check.checkArg(name);
        if (this.name == null || !this.name.getName().equals(name.getName())) {
        	dirty = true;
        }
        this.name = reparent(this.name, name);
    }

    /* (non-Javadoc)
     * @see v9t9.tools.decomp.expr.IAstNameHolder#getRoleForName()
     */
    public int getRoleForName() {
        return NAME_REFERENCED;
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
        return expr instanceof IAstIdExpression
        && ((IAstIdExpression) expr).getName().getName().equals(getName().getName());
    }
}
