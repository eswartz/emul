/*
  SymbolTable.java

  (c) 2008-2011 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
 */
package v9t9.tools.asm.assembler;

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
		return (Symbol[]) nameTable.values().toArray(new Symbol[nameTable.values().size()]);
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
