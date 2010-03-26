/**
 * 
 */
package org.ejs.eulang.ast;

import v9t9.tools.ast.expr.IAstNode;
import v9t9.tools.ast.expr.IScope;
import v9t9.tools.ast.expr.impl.AstNode;

/**
 * @author ejs
 *
 */
public class AstScope extends AstNode implements IAstScope {
	private IScope scope;

	/**
	 * 
	 */
	public AstScope(IScope scope) {
		this.scope = scope;
		scope.setOwner(this);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.IAstNode#getChildren()
	 */
	@Override
	public IAstNode[] getChildren() {
		return scope.getNodes();
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.IAstNode#getReferencedNodes()
	 */
	@Override
	public IAstNode[] getReferencedNodes() {
		return getChildren();
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
					((IAstScope) node).getScope().setParent(scope);
					break;
				}
				node = node.getParent();
			}
		} else {
			scope.setParent(null);
		}
		
	}
	
}
