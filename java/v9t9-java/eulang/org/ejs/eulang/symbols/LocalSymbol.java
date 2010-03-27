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

	public LocalSymbol(IAstName name, IAstNode def) {
		super(name, def);
	}

	public LocalSymbol(String name, IScope scope, IAstNode def) {
		super(name, scope, def);
	}

}
