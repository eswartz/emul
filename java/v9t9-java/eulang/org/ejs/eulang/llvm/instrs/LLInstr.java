/**
 * 
 */
package org.ejs.eulang.llvm.instrs;

import java.util.Set;

import org.ejs.eulang.llvm.ILLCodeVisitor;
import org.ejs.eulang.llvm.LLBlock;
import org.ejs.eulang.llvm.ops.LLOperand;

/**
 * @author ejs
 *
 */
public interface LLInstr {
	/** Set when an instr is forced to use i32 in llvm but it really only needs a target int */
	String FLAG_USE_INT_TYPE = "useIntType";
	
	int getNumber();
	void setNumber(int number);
	
	String toString();
	String getName();
	
	/** get the fixed operands : anything optional must be a new getter */
	LLOperand[] getOperands();
	void accept(LLBlock block, ILLCodeVisitor visitor);
	
	/** Flags in this interface FLAG_xxx */
	Set<String> flags();
	boolean hasFlag(String flag);
}
