/**
 * 
 */
package org.ejs.eulang;

import org.ejs.eulang.ast.ASTException;
import org.ejs.eulang.ast.IAstBinExpr;
import org.ejs.eulang.ast.IAstTypedExpr;
import org.ejs.eulang.llvm.ILLCodeTarget;
import org.ejs.eulang.llvm.LLVMGenerator;
import org.ejs.eulang.llvm.ops.LLOperand;
import org.ejs.eulang.types.LLType;
import org.ejs.eulang.types.TypeException;

/**
 * @author ejs
 *
 */
public interface IBinaryOperation extends IOperation {
	class OpTypes {
		public LLType left;
		public boolean leftIsSymbol;
		public LLType right;
		public boolean rightIsSymbol;
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

	/**
	 * Make sure the types can be generated.
	 * @param typeEngine
	 * @param types
	 */
	void validateTypes(TypeEngine typeEngine, OpTypes types) throws TypeException;

	LLOperand generate(LLVMGenerator generator, ILLCodeTarget currentTarget, IAstBinExpr expr) throws ASTException;
	/** Only allowed with operations that don't have sequencing issues (e.g. || and &&) 
	 * @param expr TODO*/
	LLOperand generate(LLVMGenerator generator, ILLCodeTarget currentTarget, IAstTypedExpr expr, LLOperand left, LLOperand right) throws ASTException;
}
