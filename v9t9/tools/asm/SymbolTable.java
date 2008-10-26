package v9t9.tools.asm;

import java.util.HashMap;

/**
 * @author ejs
 *
 */
public class SymbolTable {

	private HashMap<String, Symbol> table;

	public SymbolTable() {
		this.table = new HashMap<String, Symbol>();
	}
	
	public Symbol findSymbol(String name) {
		return table.get(name.toLowerCase());
	}
	
	/** Define a new symbol */
	public Symbol defineSymbol(String name) {
		String nameKey = name.toLowerCase();
		if (table.containsKey(nameKey))
			throw new IllegalArgumentException("Redefining symbol " + name);
		Symbol symbol = new Symbol(name);
		table.put(nameKey, symbol);
		return symbol;
	}

	public void addSymbol(Symbol symbol) {
		if (table.containsKey(symbol.getName()))
			throw new IllegalArgumentException("Redefining symbol " + symbol);
		table.put(symbol.getName().toLowerCase(), symbol);
	}
}
