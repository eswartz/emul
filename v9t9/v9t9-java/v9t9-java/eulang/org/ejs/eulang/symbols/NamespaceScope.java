/**
 * 
 */
package org.ejs.eulang.symbols;

/**
 * This is a named scope contained in a module scope.  It creates global symbols which are
 * private to the module.
 * @author ejs
 *
 */
public class NamespaceScope extends Scope {

	public NamespaceScope(IScope parent) {
		super(parent, ISymbol.Visibility.NAMESPACE);
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.symbols.IScope#newInstance()
	 */
	@Override
	public NamespaceScope newInstance(IScope parent) {
		return new NamespaceScope(parent);
	}

}
