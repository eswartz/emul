/**
 * 
 */
package org.ejs.eulang.symbols;

import org.ejs.eulang.ITyped;
import org.ejs.eulang.ast.IAstNode;

/**
 * @author ejs
 *
 */
public interface ISymbol extends ITyped {
	String getName();
	String getLLVMName();
	String getUniqueName();
	
	IScope getScope();
	void setScope(IScope scope);
	IAstNode getDefinition();
	void setDefinition(IAstNode def);
	
	boolean isTemporary();
	void setTemporary(boolean temp);

	boolean isAddressed();
	void setAddressed(boolean addressed);
	
	/**
	 * Copy self (type and name)
	 * @return
	 */
	ISymbol newInstance();
}
