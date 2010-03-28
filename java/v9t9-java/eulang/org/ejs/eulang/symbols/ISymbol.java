/**
 * 
 */
package org.ejs.eulang.symbols;

import org.ejs.eulang.ast.IAstName;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.ITyped;
import org.ejs.eulang.types.LLType;

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
}
