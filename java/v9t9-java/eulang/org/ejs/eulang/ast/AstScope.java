/**
 * 
 */
package org.ejs.eulang.ast;

import java.util.LinkedHashMap;
import java.util.Map;

import v9t9.tools.ast.expr.IAstName;
import v9t9.tools.ast.expr.IAstNameHolder;
import v9t9.tools.ast.expr.IAstNode;
import v9t9.tools.ast.expr.IScope;
import v9t9.tools.ast.expr.impl.AstNode;

/**
 * @author ejs
 *
 */
public class AstScope extends AstNode implements IAstScope {
	private IScope scope;
	private Map<IAstName, IAstNode> entries = new LinkedHashMap<IAstName, IAstNode>();
	private IAstNode owner;
	private IAstName name;

	/**
	 * 
	 */
	public AstScope(IScope scope) {
		this.scope = scope;
	}
	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.IAstNode#getChildren()
	 */
	@Override
	public IAstNode[] getChildren() {
		return entries.values().toArray(new IAstNode[0]);
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
			if (node instanceof IAstNameHolder) {
				IAstNameHolder namedParent = (IAstNameHolder) node;
				if (namedParent.getRoleForName() == IAstNameHolder.NAME_DEFINED)
					scope.setScopeName(namedParent.getName());
			}
			while (node != null) {
				if (node instanceof IAstScope) {
					((IAstScope) node).getScope().setParent(scope);
					break;
				}
				node = node.getParent();
			}
		} else {
			scope.setScopeName(null);
			scope.setParent(null);
		}
		
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstScope#add(v9t9.tools.ast.expr.IAstName, v9t9.tools.ast.expr.IAstNode)
	 */
	@Override
	public void add(IAstName name, IAstNode node) {
		scope.add(name);
		entries.put(name, node);
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstScope#find(v9t9.tools.ast.expr.IAstName)
	 */
	@Override
	public IAstNode find(IAstName name) {
		if (name.getScope().equals(this))
			return entries.get(name);
		else {
			IAstNode parent = getParent();
			while (parent != null) {
				if (parent instanceof IAstScope)
					return ((IAstScope) parent).find(name);
			}
			return null;
		}
	}
	
}
