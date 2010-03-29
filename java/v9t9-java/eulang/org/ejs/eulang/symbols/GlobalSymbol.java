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

	public GlobalSymbol(int number, IAstName name, IAstNode def) {
		super(number, name, def);
	}

	public GlobalSymbol(int number, String name, IScope scope, IAstNode def) {
		super(number, name, scope, def);
	}

}
