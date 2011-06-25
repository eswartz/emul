/**
 * 
 */
package org.ejs.eulang.symbols;

/**
 * This is a top-level scope above module scope where external symbols are
 * declared and are visible to anything linked in. 
 * @author ejs
 *
 */
public class GlobalScope extends Scope {

	public GlobalScope() {
		super(null, ISymbol.Visibility.GLOBAL);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.symbols.IScope#newInstance()
	 */
	@Override
	public GlobalScope newInstance(IScope parent) {
		return new GlobalScope();
	}

}
