/**
 * 
 */
package org.ejs.eulang.llvm.instrs;

import org.ejs.eulang.llvm.ops.LLId;
import org.ejs.eulang.llvm.ops.LLOperand;
import org.ejs.eulang.llvm.types.LLType;

/**
 * @author ejs
 *
 */
public abstract class LLBinaryInstr extends LLTypedInstr {
	
	/**
	 * Create with ops= {ret, op1, op2}; 
	 */
	public LLBinaryInstr(String name, LLType type, LLOperand... ops) {
		super(name, type, ops);
		if (ops.length != 3 || !(ops[0] instanceof LLId))
			throw new IllegalArgumentException();
	}
	
	/**  Return value:  llGetOperands()[0] */
	public LLId ret() { return (LLId) ops[0]; }
	/**  llGetOperands()[1] */
	public LLOperand op1() { return ops[1]; }
	/**  llGetOperands()[2] */
	public LLOperand op2() { return ops[2]; }
}
