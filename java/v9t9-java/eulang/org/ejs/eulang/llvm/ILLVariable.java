package org.ejs.eulang.llvm;

import org.ejs.eulang.llvm.ops.LLOperand;
import org.ejs.eulang.llvm.ops.LLSymbolOp;
import org.ejs.eulang.llvm.ops.LLVariableOp;
import org.ejs.eulang.symbols.ISymbol;

/** 
 * This is a realized variable originally specified as an ISymbol in the AST.
 * <p>
 * For LLVM, we assume that every variable is accessible in memory, which
 * is a requirement if it is ever assigned more than once (temporaries cannot
 * be reassigned, only memory can).
 * <p>
 * This interface controls code generation for the variable. 
 * @author ejs
 *
 */
public interface ILLVariable {
	/** Get the associated symbol */
	ISymbol getSymbol();
	
	/** Emit instructions to allocate the variable, and return its symbol op */
	void allocate(ILLCodeTarget target, LLOperand value);
	/** Emit instructions to destroy the variable once out of scope */
	void deallocate(ILLCodeTarget target);
	
	/** Emit instructions to load the current value into a temporary. */
	LLOperand load(ILLCodeTarget target);
	/** Emit instructions to store the given value into the variable's storage. */
	void store(ILLCodeTarget target, LLOperand value);
	/** Emit instructions to fetch the address of the variable's storage. */
	LLOperand address(ILLCodeTarget target);
}