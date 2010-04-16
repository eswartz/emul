/**
 * 
 */
package org.ejs.eulang.llvm;

import java.util.List;

import org.ejs.eulang.ITarget;
import org.ejs.eulang.llvm.instrs.LLBaseInstr;
import org.ejs.eulang.llvm.ops.LLOperand;
import org.ejs.eulang.llvm.ops.LLSymbolOp;
import org.ejs.eulang.symbols.ISymbol;
import org.ejs.eulang.types.LLType;

/**
 * Generic target for generated LLVM code.
 * @author ejs
 *
 */
public interface ILLCodeTarget {

	/**
	 * @return the blocks
	 */
	List<LLBlock> blocks();

	/**
	 * Create a new block whose label is the given symbol
	 */
	LLBlock addBlock(ISymbol symbol);

	LLBlock getCurrentBlock();

	void setCurrentBlock(LLBlock block);

	/**
	 * Get the next temporary id.  These must be used in order.
	 * @return
	 */
	ISymbol newTempSymbol();

	/**
	 * Create a new temporary operand.
	 */
	LLSymbolOp newTemp(LLType type);
	
	
	public void emit( LLBaseInstr instr);
	/**
	 * Copy a value into the target.  Target should be a variable
	 * or an address.
	 */
	public void store(LLType valueType, LLOperand value, LLOperand target);

	/**
	 * Load the value of the given source, which should be a variable or an address.
	 */
	public LLOperand load(LLType valueType, LLOperand source);

	ITarget getTarget();

	/**
	 * @return
	 */
	LLModule getModule();

	/**
	 * @return
	 */
	LLBlock getPreviousBlock();
}