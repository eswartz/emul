/**
 * 
 */
package org.ejs.eulang.types;

import java.util.Set;

import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstTypedExpr;
import org.ejs.eulang.symbols.ISymbol;

/**
 * A field in a data declaration.  This instance may be held in only one place
 * for a given instantiation of the data that originally defined it.
 * @author ejs
 *
 */
public class LLStaticField extends BaseLLField {

	private ISymbol symbol;
	
	/**
	 * Create a field, passing the 'def' IAstNode for reference/errors only
	 * @param name
	 * @param type 
	 * @param def definition of field
	 * @param defaul the default value
	 */
	public LLStaticField(String name, LLType type, ISymbol symbol, IAstNode def, IAstTypedExpr defaul, Set<String> attrs) {
		super(name, type, def, defaul, attrs);
		this.symbol = symbol;
		symbol.setType(type);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return symbol + ": " + super.toString();
	}
	
	/**
	 * @return the symbol
	 */
	public ISymbol getSymbol() {
		return symbol;
	}
}
