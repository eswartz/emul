/**
 * 
 */
package org.ejs.eulang.llvm.ops;

import org.ejs.eulang.llvm.ILLCodeVisitor;
import org.ejs.eulang.llvm.instrs.LLInstr;
import org.ejs.eulang.types.LLType;

/**
 * @author ejs
 *
 */
public interface LLOperand {
	boolean equals(Object obj);
	int hashCode();
	
	String toString();
	
	LLType getType();
	/**
	 * @return
	 */
	boolean isConstant();
	/**
	 * @param type
	 */
	void setType(LLType type);
	void accept(LLInstr instr, int num, ILLCodeVisitor visitor);
	
}
