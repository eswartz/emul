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
public interface IUnaryOperation extends IOperation {
	/**
	 * Get the type preferred for the input to this operation.
	 * @param typeEngine
	 * @param lhsType if non-<code>null</code>, the existing type into which the result is used
	 * @param opType if non-<code>null</code>, current operand type
	 * @return llType
	 */
	LLType getPreferredType(TypeEngine typeEngine, LLType lhsType, LLType opType);

	/**
	 * Get the type generated from the given input type.
	 * @param type
	 * @return
	 */
	LLType getResultType(LLType type) throws TypeException;


}
