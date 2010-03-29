/**
 * 
 */
package org.ejs.eulang.symbols;

/**
 * @author ejs
 *
 */
public class LocalScope extends Scope {

	/**
	 * @param parent
	 */
	public LocalScope(IScope parent) {
		super(parent);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.symbols.Scope#createSymbol(java.lang.String)
	 */
	@Override
	public ISymbol createSymbol(String name) {
		return new LocalSymbol(nextId(), name, this, null);
	}

}
