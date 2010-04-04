/**
 * 
 */
package org.ejs.eulang.symbols;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.ejs.eulang.ast.IAstName;
import org.ejs.eulang.ast.IAstNode;


/**
 * @author ejs
 *
 */
public abstract class Scope implements IScope {
	private Map<String, ISymbol> entries = new LinkedHashMap<String, ISymbol>();
	private IAstNode owner;
	//private String scopeName;
	private IScope parent;

	private AtomicInteger counter;
	public abstract ISymbol createSymbol(String name, boolean temporary);
	
	/**
	 * @param currentScope
	 */
	public Scope(IScope parent) {
		if (parent instanceof Scope) {
			counter = ((Scope) parent).counter;
		} else {
			counter = new AtomicInteger();
		}
		setParent(parent);
	}
	
	public int nextId() {
		return counter.getAndIncrement();
	}
	
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((entries == null) ? 0 : entries.hashCode());
		result = prime * result + ((owner == null) ? 0 : owner.getId());
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
		Scope other = (Scope) obj;
		if (entries == null) {
			if (other.entries != null)
				return false;
		} else if (!entries.equals(other.entries))
			return false;
		if (owner == null) {
			if (other.owner != null)
				return false;
		} else if (owner.getId() != other.owner.getId())
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.impl.AstNode#toString()
	 */
	@Override
	public String toString() {
		return //scopeName != null ? scopeName : 
			getOwner() != null ? getOwner().toString() : null;
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.IScope#add(v9t9.tools.ast.expr.IAstName)
	 */
	@Override
	public ISymbol add(ISymbol symbol) {
		symbol.setScope(this);
		entries.put(symbol.getName(), symbol);
		return symbol;
	}
	@Override
	public ISymbol add(IAstName name) {
		name.setScope(this);
		ISymbol symbol = createSymbol(name.getName(), false);
		entries.put(name.getName(), symbol);
		return symbol;
	}
	@Override
	public ISymbol add(IAstName name, IAstNode node) {
		ISymbol symbol = add(name);
		symbol.setDefinition(node);
		return symbol;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.symbols.IScope#remove(org.ejs.eulang.symbols.ISymbol)
	 */
	@Override
	public void remove(ISymbol symbol) {
		symbol.setDefinition(null);
		entries.remove(symbol.getName());
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.IScope#find(java.lang.String)
	 */
	@Override
	public ISymbol get(String name) {
		return entries.get(name);
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.symbols.IScope#getNode(java.lang.String)
	 */
	@Override
	public IAstNode getNode(String name) {
		ISymbol symbol = get(name);
		if (symbol == null) return null;
		return symbol.getDefinition();
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.IScope#getOwner()
	 */
	@Override
	public IAstNode getOwner() {
		return owner;
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.IScope#search(java.lang.String)
	 */
	@Override
	public ISymbol search(String name) {
		ISymbol match = get(name);
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
		if (parent != null && parent == this)
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
	 * @see org.ejs.eulang.ast.IScope#getSymbols()
	 */
	@Override
	public ISymbol[] getSymbols() {
		return (ISymbol[]) entries.values().toArray(new ISymbol[entries.values().size()]);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<ISymbol> iterator() {
		return entries.values().iterator();
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.symbols.IScope#contains(org.ejs.eulang.symbols.ISymbol)
	 */
	@Override
	public boolean contains(ISymbol symbol) {
		return entries.values().contains(symbol);
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.symbols.IScope#addTemporary(java.lang.String)
	 */
	@Override
	public ISymbol addTemporary(String name) {
		ISymbol symbol = createSymbol(name + "@" + counter.get(), true);
		entries.put(name, symbol);
		return symbol;		
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.symbols.IScope#encloses(org.ejs.eulang.symbols.IScope)
	 */
	@Override
	public boolean encloses(IScope otherScope) {
		while (otherScope != null) {
			if (otherScope == this)
				return true;
			otherScope = otherScope.getParent();
		}
		return false;
	}
}
