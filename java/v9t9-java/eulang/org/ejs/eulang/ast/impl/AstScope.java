/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstScope;
import org.ejs.eulang.ast.IAstStmtScope;
import org.ejs.eulang.symbols.IScope;


/**
 * @author ejs
 *
 */
public abstract class AstScope extends AstNode implements IAstScope {
	protected IScope scope;

	/**
	 * 
	 */
	public AstScope(IScope scope) {
		this.scope = scope;
		scope.setOwner(this);
	}
   
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 22;
		result = prime * result + ((scope == null) ? 0 : scope.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;
		AstScope other = (AstScope) obj;
		if (!scope.equals(other.scope))
			return false;
		return true;
	}

	protected IAstScope fixupScope(IAstScope copied) {
		remapScope(getScope(), copied.getScope(), copied);
		return fixup(this, copied);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstScope#getScope()
	 */
	@Override
	public IScope getScope() {
		return scope;
	}
	
	@Override
	public void setParent(IAstNode node) {
		super.setParent(node);

		if (node != null) {
			while (node != null) {
				if (node instanceof IAstScope) {
					scope.setParent(((IAstScope) node).getScope());
					break;
				}
				node = node.getParent();
			}
		} else {
			scope.setParent(null);
		}
		
	}
	
}
