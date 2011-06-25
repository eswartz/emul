/**
 * 
 */
package org.ejs.eulang;

import org.ejs.eulang.ast.IAstLitExpr;
import org.ejs.eulang.ast.IAstUnaryExpr;
import org.ejs.eulang.llvm.ops.LLConstOp;
import org.ejs.eulang.types.LLType;
import org.ejs.eulang.types.TypeException;

/**
 * @author ejs
 *
 */
public interface IUnaryOperation extends IOperation {
	class OpTypes {
		public LLType expr;
		public LLType result;
	}
	
	/**
	 * Update the types to those the operation needs.  Some or all of 
	 * 'types' may be null.  If the incoming types are inappropriate, throws an exception.  
	 */
	void inferTypes(TypeEngine typeEngine, OpTypes types) throws TypeException;

	/**
	 * Update 'expr' to the type expected by 'result'.  All the entries
	 * in 'types' are set.
	 * @param expr 
	 * @param typeEngine
	 * @param types
	 * @return true if changed structure 
	 */
	boolean transformExpr(IAstUnaryExpr expr, TypeEngine typeEngine, OpTypes types) throws TypeException;

	/**
	 * Ensure these types work
	 * @param typeEngine
	 * @param types
	 */
	void validateTypes(TypeEngine typeEngine, OpTypes types) throws TypeException;

	/**
	 * Evaluate constant 
	 * @param llType destination type
	 * @param simExpr
	 * @return
	 */
	LLConstOp evaluate(LLType llType, IAstLitExpr simExpr);
}
