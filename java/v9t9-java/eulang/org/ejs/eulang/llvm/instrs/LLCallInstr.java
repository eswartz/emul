/**
 * 
 */
package org.ejs.eulang.llvm.instrs;

import org.ejs.eulang.llvm.ops.LLOperand;
import org.ejs.eulang.types.LLCodeType;
import org.ejs.eulang.types.LLType;
import org.ejs.eulang.types.LLVoidType;

/**
 * @author ejs
 *
 */
public class LLCallInstr extends LLAssignInstr {

	private final LLOperand func;
	private final LLCodeType funcType;

	/**
	 * @param name
	 * @param type
	 * @param funcType 
	 * @param ops
	 */
	public LLCallInstr(LLOperand ret, LLType type, LLOperand func, LLCodeType funcType, LLOperand... ops) {
		super("call", ret, type, ops);
		this.func = func;
		this.funcType = funcType;
		if (type instanceof LLVoidType) {
			if (ret != null)
				throw new IllegalArgumentException();
		} else {
			if (ret == null)
				throw new IllegalArgumentException();
		}
			
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.instrs.LLTypedInstr#appendOptionString(java.lang.StringBuilder)
	 */
	@Override
	protected void appendOptionString(StringBuilder sb) {
		super.appendOptionString(sb);
		sb.append(func).append(" (");
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.instrs.LLAssignInstr#appendInstrString(java.lang.StringBuilder)
	 */
	@Override
	protected void appendInstrString(StringBuilder sb) {
		super.appendInstrString(sb);
		sb.append(')');
	}
	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.instrs.LLBaseInstr#appendOperandString(java.lang.StringBuilder, int, org.ejs.eulang.llvm.ops.LLOperand)
	 */
	@Override
	protected void appendOperandString(StringBuilder sb, int idx, LLOperand op) {
		sb.append(funcType.getArgTypes()[idx].getLLVMName()).append(' ');
		super.appendOperandString(sb, idx, op);
	}
}
