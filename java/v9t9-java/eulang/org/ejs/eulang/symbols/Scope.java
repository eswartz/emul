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
import org.ejs.eulang.ast.IAstSymbolDefiner;
import org.ejs.eulang.symbols.ISymbol.Visibility;


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
	private final Visibility defaultVisibility;
	
	private int uniquifyId;
	
	public ISymbol createSymbol(String name, boolean temporary) {
		return new Symbol(nextId(), name, defaultVisibility, null, temporary, this, null, false);
	}
	
	/**
	 * @param currentScope
	 */
	public Scope(IScope parent, Visibility vis) {
		this.defaultVisibility = vis;
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
	 * @see org.ejs.eulang.symbols.IScope#add(java.lang.String)
	 */
	@Override
	public ISymbol add(String symName, boolean uniquify) {
		if (entries.containsKey(symName)) {
			if (uniquify)
				symName += "." + uniquifyId++;
			else
				throw new IllegalArgumentException();
		}
		ISymbol symbol = createSymbol(symName, false);
		entries.put(symName, symbol);
		return symbol;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.IScope#add(v9t9.tools.ast.expr.IAstName)
	 */
	@Override
	public ISymbol add(ISymbol symbol) {
		if (entries.containsKey(symbol.getUniqueName()))
			throw new IllegalArgumentException();
		symbol.setScope(this);
		entries.put(symbol.getUniqueName(), symbol);
		counter.set(Math.max(counter.get(), symbol.getNumber() + 1));
		return symbol;
	}
	@Override
	public ISymbol add(IAstName name) {
		if (entries.containsKey(name.getName()))
			throw new IllegalArgumentException();

		name.setScope(this);
		ISymbol symbol = createSymbol(name.getName(), false);
		entries.put(name.getName(), symbol);
		return symbol;
	}
	/* (non-Javadoc)
	 * @see org.ejs.eulang.symbols.IScope#add(org.ejs.eulang.symbols.ISymbol.Visibility, org.ejs.eulang.ast.IAstName)
	 */
	@Override
	public ISymbol add(Visibility vis, IAstName name) {
		ISymbol symbol = add(name);
		symbol.setVisibility(vis);
		return symbol;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.symbols.IScope#remove(org.ejs.eulang.symbols.ISymbol)
	 */
	@Override
	public void remove(ISymbol symbol) {
		entries.remove(symbol.getUniqueName());
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
		if (counter == null) {
			if (parent instanceof Scope) {
				counter = ((Scope) parent).counter;
			} else {
				counter = new AtomicInteger();
			}
		}
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
		//if (uniquifyt)
		//	name += "@" + counter.get();
		ISymbol symbol = createSymbol(name, true);
		entries.put(symbol.getUniqueName(), symbol);
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
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.symbols.IScope#copySymbol(org.ejs.eulang.symbols.ISymbol)
	 */
	@Override
	public ISymbol copySymbol(ISymbol symbol) {
		ISymbol copySymbol = new Symbol(symbol.getNumber(), symbol.getName(), 
					symbol.getVisibility(), symbol.getType(), 
					symbol.isTemporary(), 
					null, symbol.getDefinition(), symbol.isAddressed());
		add(copySymbol);
		copySymbol.setType(symbol.getType());
		return copySymbol;
	}
	

	/* (non-Javadoc)
	 * @see org.ejs.eulang.symbols.IScope#getUniqueName()
	 */
	@Override
	public String getUniqueName() {
		StringBuilder sb = new StringBuilder();
		getScopePrefix(sb, this);
		return sb.toString();
	}
	/**
	 * @param sb 
	 * @param scope
	 * @return
	 */
	private void getScopePrefix(StringBuilder sb, IScope scope) {
		if (scope == null)
			return;
		getScopePrefix(sb, scope.getParent());
		if (scope.getOwner() != null && scope.getOwner() instanceof IAstSymbolDefiner) {
			ISymbol scopeSymbol = ((IAstSymbolDefiner) scope.getOwner()).getSymbol();
			if (scopeSymbol != null)
				sb.append(scopeSymbol.getName()).append('.');
		}
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.symbols.IScope#get(int)
	 */
	@Override
	public ISymbol get(int number) {
		for (ISymbol sym : entries.values())
			if (sym.getNumber() == number)
				return sym;
		return null;
	}
}
