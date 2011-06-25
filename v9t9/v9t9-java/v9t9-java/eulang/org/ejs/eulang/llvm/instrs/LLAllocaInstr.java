/**
 * 
 */
package org.ejs.eulang.llvm.instrs;

import org.ejs.eulang.llvm.ops.LLOperand;
import org.ejs.eulang.types.LLType;

/**
 * @author ejs
 *
 */
public class LLAllocaInstr extends LLAssignInstr {

	/**
	 * @type is the actual type desired (not pointer to...)
	 */
	public LLAllocaInstr(LLOperand ret, LLType type, LLOperand... ops) {
		super("alloca", ret, type, ops);
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.instrs.LLBaseInstr#noCommaBeforeOperands()
	 */
	@Override
	protected boolean noCommaBeforeOperands() {
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.instrs.LLBaseInstr#appendOperandString(java.lang.StringBuilder, int, org.ejs.eulang.llvm.ops.LLOperand)
	 */
	@Override
	protected void appendOperandString(StringBuilder sb, int idx, LLOperand op) {
		sb.append(op.getType().getLLVMType()).append(' ');
		super.appendOperandString(sb, idx, op);
	}

}
