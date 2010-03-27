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
public interface IBinaryOperation extends IOperation {
	/**
	 * Get the type preferred for the left-hand side.  For instance, a logical
	 * operation wants an integer.  
	 * @param typeEngine 
	 * @param lhsType if non-<code>null</code>, the known type into which the result is typed
	 * @param leftType if non-<code>null</code>, the existing left-hand side
	 * @param rightType if non-<code>null</code>, the existing right-hand side
	 * @return type
	 * @throws TypeException
	 */
	LLType getPreferredLeftType(TypeEngine typeEngine, LLType lhsType, LLType leftType, LLType rightType);
	
	/** 
	 * Get the type preferred for the right-hand side.  For instance, a shift
	 * wants an integral count.  The returned type may have 0 bits to indicate any
	 * size is okay, otherwise, it will have a specific count.
	 * @param typeEngine 
	 * @param lhsType if non-<code>null</code>, the known type into which the result is typed
	 * @param leftType if non-<code>null</code>, the existing left-hand side
	 * @param rightType if non-<code>null</code>, the existing right-hand side
	 * @return type
	 */
	LLType getPreferredRightType(TypeEngine typeEngine, LLType lhsType, LLType leftType, LLType rightType);
	
	/**
	 * Get the type generated from the given input types.
	 * @param typeEngine 
	 * @param leftType
	 * @param rightType
	 * @return new type
	 */
	LLType getResultType(TypeEngine typeEngine, LLType leftType, LLType rightType) throws TypeException;
}
