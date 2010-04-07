/**
 * 
 */
package org.ejs.eulang;

import org.ejs.eulang.llvm.ILLCodeTarget;
import org.ejs.eulang.llvm.ops.LLOperand;
import org.ejs.eulang.types.LLType;

/**
 * The compilation target
 * @author ejs
 *
 */
public interface ITarget {
	TypeEngine getTypeEngine();
	
	/** e.g. "ccc", "fastcc", "cc &lt;n&gt;" */
	String getLLCallingConvention();

	/**
	 * Get the GNU-style target triple
	 * @return e.g. "foo-bar-baz"
	 */
	String getTriple();
	
	/**
	 * Increment a reference to a ref-counted object with the given id (may be 0)
	 * @param target TODO
	 * @param valueType TODO
	 * @param value
	 */
	void incRef(ILLCodeTarget target, LLType valueType, LLOperand value);

	/**
	 * Decrement a reference to a ref-counted object with the given id (may be  0)
	 * @param target TODO
	 * @param valueType TODO
	 * @param value
	 */
	void decRef(ILLCodeTarget target, LLType valueType, LLOperand value);
}
