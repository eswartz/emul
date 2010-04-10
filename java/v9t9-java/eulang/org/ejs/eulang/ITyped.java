/**
 * 
 */
package org.ejs.eulang;

import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.types.LLType;

/**
 * @author ejs
 *
 */
public interface ITyped {

	LLType getType();

	void setType(LLType type);
	
	/** Get a responsible node, e.g. for error reporting */
	IAstNode getNode();

}