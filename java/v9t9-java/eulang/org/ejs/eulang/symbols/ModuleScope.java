/**
 * 
 */
package org.ejs.eulang.symbols;

/**
 * @author ejs
 *
 */
public class ModuleScope extends Scope {

	/**
	 * @param parent
	 */
	public ModuleScope(IScope parent) {
		super(parent);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.symbols.Scope#createSymbol(java.lang.String)
	 */
	@Override
	public ISymbol createSymbol(String name) {
		return new GlobalSymbol(nextId(), name, this, null);
	}

}
