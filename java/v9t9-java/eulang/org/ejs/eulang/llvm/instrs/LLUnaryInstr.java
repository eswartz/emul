/**
 * 
 */
package org.ejs.eulang.llvm.instrs;

import org.ejs.eulang.IOperation;
import org.ejs.eulang.llvm.ops.LLOperand;
import org.ejs.eulang.types.LLType;

/**
 * @author ejs
 *
 */
public class LLUnaryInstr extends LLAssignInstr {
	
	private final IOperation op;
	/**
	 * Create with ops= {ret, op1, op2}; 
	 */
	public LLUnaryInstr(IOperation op, LLOperand ret, LLType type, LLOperand... ops) {
		super(op.getLLVMName(), ret, type, ops);
		this.op = op;
		if (ops.length != 1)
			throw new IllegalArgumentException();
	}
	
	/**  Return value:  llGetOperands()[0] */
	public LLOperand ret() { return (LLOperand) ops[0]; }
	/**  llGetOperands()[1] */
	public LLOperand op() { return ops[1]; }
	
	
}
