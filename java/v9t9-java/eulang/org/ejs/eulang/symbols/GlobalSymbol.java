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
public class GlobalSymbol extends BaseSymbol {

	public GlobalSymbol(IAstName name, IAstNode def) {
		super(name, def);
	}

	public GlobalSymbol(String name, IScope scope, IAstNode def) {
		super(name, scope, def);
	}

}
