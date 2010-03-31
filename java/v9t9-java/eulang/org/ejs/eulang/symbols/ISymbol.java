/**
 * 
 */
package org.ejs.eulang.symbols;

import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.ITyped;

/**
 * @author ejs
 *
 */
public interface ISymbol extends ITyped {
	String getName();
	IScope getScope();
	void setScope(IScope scope);
	IAstNode getDefinition();
	void setDefinition(IAstNode def);
	
	boolean isTemporary();
	void setTemporary(boolean temp);
	
	/**
	 * Copy self (type and name)
	 * @return
	 */
	ISymbol newInstance();
}
