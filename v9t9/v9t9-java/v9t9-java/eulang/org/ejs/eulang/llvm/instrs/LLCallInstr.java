/**
 * 
 */
package org.ejs.eulang.llvm.instrs;

import org.ejs.eulang.llvm.ILLCodeVisitor;
import org.ejs.eulang.llvm.LLBlock;
import org.ejs.eulang.llvm.ops.LLOperand;
import org.ejs.eulang.types.LLCodeType;
import org.ejs.eulang.types.LLPointerType;
import org.ejs.eulang.types.LLSymbolType;
import org.ejs.eulang.types.LLType;
import org.ejs.eulang.types.LLVoidType;

/**
 * @author ejs
 *
 */
public class LLCallInstr extends LLAssignInstr {

	private final LLOperand func;

	/**
	 * @param type
	 * @param ops
	 * @param name
	 */
	public LLCallInstr(LLOperand ret, LLType type, LLOperand func, LLOperand... ops) {
		super("call", ret, type, ops);
		this.func = func;
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
		LLType funcType = func.getType();
		if (funcType instanceof LLPointerType)
			funcType = funcType.getSubType();
		if (funcType instanceof LLSymbolType)
			sb.append("<sym> ");
		else {
			sb.append(((LLCodeType)funcType).getArgTypes()[idx].getLLVMName()).append(' ');
		}
		super.appendOperandString(sb, idx, op);
	}
	
	/**
	 * @return the func
	 */
	public LLOperand getFunction() {
		return func;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.instrs.LLBaseInstr#accept(org.ejs.eulang.llvm.LLBlock, org.ejs.eulang.llvm.ILLCodeVisitor)
	 */
	@Override
	public void accept(LLBlock block, ILLCodeVisitor visitor) {
		super.accept(block, visitor);
		func.accept(this, -2, visitor);
	}
}
