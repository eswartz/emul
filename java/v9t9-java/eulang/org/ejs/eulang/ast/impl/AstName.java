/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.eulang.ast.IAstName;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.symbols.IScope;

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
     * @see org.ejs.eulang.ast.IAstNode#copy()
     */
    @Override
    public IAstName copy(IAstNode copyParent) {
    	return fixup(this, new AstName(getName(), null));
    }
    
    
    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((scope == null) ? 0 : scope.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AstName other = (AstName) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (scope == null) {
			if (other.scope != null)
				return false;
		} else if (!scope.equals(other.scope))
			return false;
		return true;
	}

	/* (non-Javadoc)
     * @see v9t9.tools.decomp.expr.impl.AstNode#toString()
     */
    @Override
	public String toString() {
        //return "AstName { name="+name+" scope="+scope + " }"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    	return name;
    }
    
    /* (non-Javadoc)
     * @see v9t9.tools.decomp.expr.IAstNode#getChildren()
     */
    public IAstNode[] getChildren() {
        return NO_CHILDREN;
    }
   
    @Override
	public void replaceChildren(IAstNode[] children) {
		throw new UnsupportedOperationException();
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
