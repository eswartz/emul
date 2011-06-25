/**
 * 
 */
package org.ejs.eulang.ast;

import org.ejs.eulang.ITyped;
import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.types.TypeException;


/**
 * @author ejs
 *
 */
public interface IAstTypedNode extends IAstNode, ITyped {
	/**
	 * Infer the type this node should have by examining its childrens' types
	 * and applying any semantics specific to the node.  This should not recurse.
	 * This should always make a new decision even if {@link #getType()} is not <code>null</code>.
	 * @param typeEngine 
	 * @return true if changes made 
	 * @throws TypeException if the inference detected illegal type combinations
	 */
	boolean inferTypeFromChildren(TypeEngine typeEngine) throws TypeException;
	
	/** Tell if the type is fixed and should not be changed */
	boolean isTypeFixed();
	
	void setTypeFixed(boolean fixed);
}
