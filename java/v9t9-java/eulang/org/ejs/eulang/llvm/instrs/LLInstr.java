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
	int getNumber();
	void setNumber(int number);
	
	String toString();
	String getName();
	
	/** get the fixed operands : anything optional must be a new getter */
	LLOperand[] getOperands();
	void accept(LLBlock block, ILLCodeVisitor visitor);
	
	Set<String> flags();
}
