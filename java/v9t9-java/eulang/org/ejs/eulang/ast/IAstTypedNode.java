/**
 * 
 */
package org.ejs.eulang.ast;

import org.ejs.eulang.types.LLType;
import org.ejs.eulang.types.TypeException;


/**
 * @author ejs
 *
 */
public interface IAstTypedNode extends IAstNode {

	LLType getType();
	void setType(LLType type);
	
	/**
	 * Infer the type this node should have by examining its childrens' types
	 * and applying any semantics specific to the node.  This should not recurse.
	 * This should always make a new decision even if {@link #getType()} is not <code>null</code>.
	 * @param typeEngine 
	 * @return LLType or <code>null</code> if children don't have types 
	 * @throws TypeException if the inference detected illegal type combinations
	 */
	LLType inferTypeFromChildren(TypeEngine typeEngine) throws TypeException;
	
	/**
	 * This is like {@link #setType(LLType)}, but called as a result of type inference.
	 * Update any other expressions that depend on this type and insert cast operations
	 * if necessary.  (Don't call #setType() on yourself.)
	 * @param typeEngine TODO
	 * @param newType
	 */
	void setTypeOnChildren(TypeEngine typeEngine, LLType newType);
}
