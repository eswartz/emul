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
public class Scope implements IScope {
	private Map<String, IAstName> entries = new LinkedHashMap<String, IAstName>();
	private IAstNode owner;
	private IAstName name;
	private IScope parent;

	/**
	 * @param currentScope
	 */
	public Scope(IScope parent) {
		setParent(parent);
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.IScope#add(v9t9.tools.ast.expr.IAstName)
	 */
	@Override
	public void add(IAstName name) {
		entries.put(name.getName(), name);
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.IScope#find(java.lang.String)
	 */
	@Override
	public IAstName find(String name) {
		return entries.get(name);
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.IScope#getOwner()
	 */
	@Override
	public IAstNode getOwner() {
		return owner;
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.IScope#getScopeName()
	 */
	@Override
	public IAstName getScopeName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.IScope#remove(v9t9.tools.ast.expr.IAstName)
	 */
	@Override
	public void remove(IAstName name) {
		entries.remove(name);
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.IScope#search(java.lang.String)
	 */
	@Override
	public IAstName search(String name) {
		IAstName match = find(name);
		if (match == null) {
			IScope up = getParent();
			if (up != null)
				return up.search(name);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.IScope#setOwner(v9t9.tools.ast.expr.IAstNode)
	 */
	@Override
	public void setOwner(IAstNode owner) {
		this.owner = owner;
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.IScope#setParent(v9t9.tools.ast.expr.IScope)
	 */
	@Override
	public void setParent(IScope parent) {
		if (parent != null && parent.equals(this))
			throw new IllegalStateException();
		this.parent = parent;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.IScope#getParent()
	 */
	@Override
	public IScope getParent() {
		return parent;
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.IScope#setScopeName(v9t9.tools.ast.expr.IAstName)
	 */
	@Override
	public void setScopeName(IAstName name) {
		this.name = name;
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.IScope#getNames()
	 */
	@Override
	public IAstName[] getNames() {
		return (IAstName[]) entries.values().toArray(new IAstName[entries.values().size()]);
	}
}
