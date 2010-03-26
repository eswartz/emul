/**
 * 
 */
package org.ejs.eulang.ast;

import java.util.LinkedHashMap;
import java.util.Map;

import v9t9.tools.ast.expr.IAstName;
import v9t9.tools.ast.expr.IAstNode;
import v9t9.tools.ast.expr.IScope;

// TODO: should IScope or IAstScope or what have the responsibility of mapping IAstName to IAstNode?
// It'll be silly to reimplement the same code in modules, code blocks, data blocks, etc...

/**
 * @author ejs
 *
 */
public class Scope implements IScope {
	private Map<String, IAstName> entries = new LinkedHashMap<String, IAstName>();
	private Map<IAstName, IAstNode> nodeEntries = new LinkedHashMap<IAstName, IAstNode>();
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
	 * @see v9t9.tools.ast.expr.impl.AstNode#toString()
	 */
	@Override
	public String toString() {
		return name != null ? name.getName() : getOwner() != null ? getOwner().toString() : null;
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.IScope#add(v9t9.tools.ast.expr.IAstName)
	 */
	@Override
	public void add(IAstName name, IAstNode node) {
		entries.put(name.getName(), name);
		nodeEntries.put(name, node);
		name.setScope(this);
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.IScope#find(java.lang.String)
	 */
	@Override
	public IAstNode findNode(String name) {
		IAstName nameNode = entries.get(name);
		if (nameNode != null)
			return nodeEntries.get(nameNode);
		else
			return null;
	}
	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.IScope#find(java.lang.String)
	 */
	@Override
	public IAstName find(String name) {
		IAstName nameNode = entries.get(name);
		return nameNode;
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
		entries.remove(name.getName());
		nodeEntries.remove(name);
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.IScope#search(java.lang.String)
	 */
	@Override
	public IAstName search(String name) {
		IAstName match = entries.get(name);
		if (match != null)
			return match;
		IScope up = getParent();
		if (up != null)
			return up.search(name);
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
	
	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.IScope#getNodes()
	 */
	@Override
	public IAstNode[] getNodes() {
		return (IAstNode[]) nodeEntries.values().toArray(new IAstNode[nodeEntries.values().size()]);
	}
}
