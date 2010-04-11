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

	public GlobalSymbol(int number, String name, boolean temporary, IScope scope, IAstNode def, boolean addressed) {
		super(number, name, temporary, scope, def, addressed);
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.symbols.ISymbol#copy()
	 */
	@Override
	public GlobalSymbol newInstance() {
		return new GlobalSymbol(getNumber(), getName(), isTemporary(), null, getDefinition(), isAddressed());
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.symbols.BaseSymbol#getLLVMPrefix()
	 */
	@Override
	protected String getLLVMPrefix() {
		return "@";
	}
	

}
