/**
 * 
 */
package org.ejs.eulang.symbols;

/**
 * This is a scope inside a code block.
 * @author ejs
 *
 */
public class LocalScope extends Scope {

	/**
	 * @param parent
	 */
	public LocalScope(IScope parent) {
		super(parent, ISymbol.Visibility.LOCAL);
	}
	
	public LocalScope newInstance(IScope parent) {
		return new LocalScope(parent);
	}

}
