/**
 * 
 */
package org.ejs.eulang.symbols;

import org.ejs.eulang.ast.IAstName;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.types.LLType;

/**
 * @author ejs
 *
 */
public interface ISymbol {
	IAstName getName();
	LLType getType();
	void setType(LLType type);
	IAstNode getDefinition();
	void setDefinition(IAstNode def);
}
