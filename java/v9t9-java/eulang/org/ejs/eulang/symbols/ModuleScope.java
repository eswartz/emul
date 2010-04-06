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
	public ISymbol createSymbol(String name, boolean temporary) {
		return new GlobalSymbol(nextId(), name, temporary, this, null, false);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.symbols.IScope#newInstance(org.ejs.eulang.symbols.IScope)
	 */
	@Override
	public ModuleScope newInstance(IScope parent) {
		return new ModuleScope(parent);
	}
}
