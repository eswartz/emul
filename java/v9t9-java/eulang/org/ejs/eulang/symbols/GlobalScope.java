/**
 * 
 */
package org.ejs.eulang.symbols;

/**
 * @author ejs
 *
 */
public class GlobalScope extends Scope {

	public GlobalScope() {
		super(null);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.symbols.Scope#createSymbol(java.lang.String)
	 */
	@Override
	public ISymbol createSymbol(String name) {
		return new GlobalSymbol(name, this, null);
	}

}
