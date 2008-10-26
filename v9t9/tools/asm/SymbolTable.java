package v9t9.tools.asm;

import java.util.HashMap;

/**
 * @author ejs
 *
 */
public class SymbolTable {

	private HashMap<String, Symbol> nameTable;
	//private HashMap<Integer, Symbol> indexTable;
	//private int symbolNumber;
	
	public SymbolTable() {
		this.nameTable = new HashMap<String, Symbol>();
		//this.indexTable = new HashMap<Integer, Symbol>();
	}
	
	public Symbol findSymbol(String name) {
		return nameTable.get(name.toLowerCase());
	}

	//public Symbol findSymbol(int index) {
	//	return indexTable.get(index);
	//}

	/** 
	 * Declare a symbol (ignoring if already declared)
	 * 
	 */
	public Symbol declareSymbol(String name) {
		Symbol symbol = findSymbol(name);
		if (symbol != null)
			return symbol;
		symbol = new Symbol(name);
		//symbol.setIndex(++symbolNumber);
		nameTable.put(name.toLowerCase(), symbol);
		//indexTable.put(symbol.getIndex(), symbol);
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
}
