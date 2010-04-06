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
	public ISymbol createSymbol(String name, boolean temporary) {
		return new LocalSymbol(nextId(), name, temporary, this, null, false);
	}
	
	public LocalScope newInstance(IScope parent) {
		return new LocalScope(parent);
	}

}
