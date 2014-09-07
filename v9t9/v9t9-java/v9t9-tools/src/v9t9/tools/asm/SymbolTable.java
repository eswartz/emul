/*
  SymbolTable.java

  (c) 2008-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools.asm;

import java.util.Arrays;
import java.util.HashMap;

/**
 * @author ejs
 *
 */
public class SymbolTable {

	private HashMap<String, Symbol> nameTable;
	private final SymbolTable parent;
	//private HashMap<Integer, Symbol> indexTable;
	//private int symbolNumber;
	
	public SymbolTable() {
		this(null);
	}
	
	public SymbolTable(SymbolTable symbolTable) {
		this.nameTable = new HashMap<String, Symbol>();
		this.parent = symbolTable;
	}

	public SymbolTable getParent() {
		return parent;
	}
	
	public Symbol findSymbolLocal(String name) {
		Symbol symbol = nameTable.get(name.toLowerCase());
		return symbol;
	}
	
	public Symbol findSymbol(String name) {
		SymbolTable scope = this;
		while (scope != null) {
			Symbol symbol = scope.findSymbolLocal(name);
			if (symbol != null)
				return symbol;
			scope = scope.getParent();
		}
		return null;
	}

	//public Symbol findSymbol(int index) {
	//	return indexTable.get(index);
	//}

	/** 
	 * Find a symbol in the scope or define a symbol in this scope.
	 * 
	 */
	public Symbol findOrCreateSymbol(String name) {
		Symbol symbol = findSymbol(name);
		if (symbol != null)
			return symbol;
		return createSymbol(name);
	}
	
	/** 
	 * Create a symbol in this scope (ignoring if already defined)
	 * 
	 */
	public Symbol createSymbol(String name) {
		Symbol symbol = new Symbol(this, name);
		nameTable.put(name.toLowerCase(), symbol);
		return symbol;
	}

	/**
	 * Add a symbol, overwriting an existing one
	 * @param symbol
	 */
	public void addSymbol(Symbol symbol) {
		nameTable.put(symbol.getName().toLowerCase(), symbol);
		//if (symbol.getIndex() == 0)
		//	symbol.setIndex(++symbolNumber);
		//indexTable.put(symbol.getIndex(), symbol);
	}

	public Symbol[] getSymbols() {
		Symbol[] syms = (Symbol[]) nameTable.values().toArray(new Symbol[nameTable.values().size()]);
		Arrays.sort(syms);
		return syms;
	}

	public void undefineSymbols() {
		for (Symbol symbol : nameTable.values()) {
			symbol.setDefined(false);
		}
		if (parent != null)
			parent.undefineSymbols();
	}

	public void clear() {
		nameTable.clear();
	}

}
