/**
 * 
 */
package org.ejs.eulang.llvm.instrs;

import org.ejs.eulang.llvm.ILLCodeVisitor;
import org.ejs.eulang.llvm.ops.LLOperand;

/**
 * @author ejs
 *
 */
public interface LLInstr {
	String toString();
	String getName();
	
	/** get the fixed operands : anything optional must be a new getter */
	LLOperand[] getOperands();
	/**
	 * @param visitor
	 */
	void accept(ILLCodeVisitor visitor);
}
