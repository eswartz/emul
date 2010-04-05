/**
 * 
 */
package org.ejs.eulang;

import org.ejs.eulang.types.LLType;
import org.ejs.eulang.types.TypeException;

/**
 * @author ejs
 *
 */
public interface IBinaryOperation extends IOperation {
	class OpTypes {
		public LLType left;
		public LLType right;
		public LLType result;
	}
	
	/**
	 * Update the types to those the operation needs.  Some or all of 
	 * 'types' may be null.  If the incoming types are inappropriate, throws an exception.  
	 */
	void inferTypes(TypeEngine typeEngine, OpTypes types) throws TypeException;

	/**
	 * Update 'left' and 'right' to the type expected by 'result'.  All the entries
	 * in 'types' are set.
	 * @param typeEngine
	 * @param types
	 */
	void castTypes(TypeEngine typeEngine, OpTypes types) throws TypeException;
}
