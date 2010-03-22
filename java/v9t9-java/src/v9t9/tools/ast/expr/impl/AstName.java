/**
 * 
 */
package v9t9.tools.ast.expr.impl;

import v9t9.tools.ast.expr.IAstName;
import v9t9.tools.ast.expr.IAstNode;
import v9t9.tools.ast.expr.IScope;

/**
 * @author eswartz
 *
 */
public class AstName extends AstNode implements IAstName {

    String name;
    IScope scope;
    
    /**
     * Create a name
     * 
     * @param name
     * @param scope
     */
    public AstName(String name, IScope scope) {
        super();
        setName(name);
        setScope(scope);
        dirty = false;
    }
    
    /**
     * @param value
     */
    public AstName(String value) {
        super();
        setName(value);
        setScope(null);
        dirty = false;
    }

    /* (non-Javadoc)
     * @see v9t9.tools.decomp.expr.impl.AstNode#toString()
     */
    @Override
	public String toString() {
        return "AstName { name="+name+" scope="+scope + " }"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
     * @see v9t9.tools.decomp.expr.impl.AstNode#constructText()
     */
    public Object[] getTextSegments() {
        return new Object[] { name };
    }

    /* (non-Javadoc)
     * @see v9t9.tools.decomp.expr.IAstName#getName()
     */
    public String getName() {
        return name;
    }

    /* (non-Javadoc)
     * @see v9t9.tools.decomp.expr.IAstName#setName(java.lang.String)
     */
    public void setName(String name) {
        org.ejs.coffee.core.utils.Check.checkArg(name);
        this.name = name;
        this.dirty = true;
    }

    /* (non-Javadoc)
     * @see v9t9.tools.decomp.expr.IAstName#getScope()
     */
    public IScope getScope() {
        return scope;
    }

    /* (non-Javadoc)
     * @see v9t9.tools.decomp.expr.IAstName#setScope(v9t9.tools.decomp.expr.IScope)
     */
    public void setScope(IScope scope) {
        this.scope = scope;
    }

 }
