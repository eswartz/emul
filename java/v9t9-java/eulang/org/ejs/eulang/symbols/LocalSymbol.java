/**
 * 
 */
package org.ejs.eulang.symbols;

import org.ejs.eulang.ast.IAstName;
import org.ejs.eulang.ast.IAstNode;

/**
 * @author ejs
 *
 */
public class LocalSymbol extends BaseSymbol {

	public LocalSymbol(int number, IAstName name, IAstNode def) {
		super(number, name, def);
	}

	public LocalSymbol(int number, String name, boolean temporary, IScope scope, IAstNode def, boolean addressed) {
		super(number, name, temporary, scope, def, addressed);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.symbols.ISymbol#copy()
	 */
	@Override
	public LocalSymbol newInstance() {
		return new LocalSymbol(getNumber(), getName(), false, null, getDefinition(), isAddressed());
	}
}
