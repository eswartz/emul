/**
 * 
 */
package org.ejs.eulang.symbols;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

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

	public abstract ISymbol createSymbol(String name);
	
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
		ISymbol symbol = createSymbol(name.getName());
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
}
