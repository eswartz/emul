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

	public LocalSymbol(int number, String name, IScope scope, IAstNode def) {
		super(number, name, scope, def);
	}

}
