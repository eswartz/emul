/**
 * 
 */
package org.ejs.eulang.symbols;

/**
 * This is a per-file scope.  It does not have a name.  Top-level symbols in the module
 * are visible to other modules via imports.
 * @author ejs
 *
 */
public class ModuleScope extends Scope {

	/**
	 * @param parent
	 */
	public ModuleScope(IScope parent) {
		super(parent, ISymbol.Visibility.MODULE);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.symbols.IScope#newInstance(org.ejs.eulang.symbols.IScope)
	 */
	@Override
	public ModuleScope newInstance(IScope parent) {
		return new ModuleScope(parent);
	}
}
